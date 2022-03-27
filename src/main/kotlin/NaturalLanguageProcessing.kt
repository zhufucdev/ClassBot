package com.zhufucdev

import com.zhufucdev.data.Database
import com.zhufucdev.data.homework.HomeworkManifest
import com.zhufucdev.data.homework.Subject
import com.zhufucdev.serialization.defaultZone
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

/**
 * Represent something that can be inferred from context
 */
abstract class Conclusion {
    val extra = arrayListOf<KnownInfo>()
}

object NothingToDo : Conclusion()

enum class Action(val regex: Regex) {
    HAND_IN_HOMEWORK(Regex("提?交下?.*的?作业")),
    AMEND_HOMEWORK(Regex("修?改下?.*的?作业")),
    REVIEW_HOMEWORK(Regex("(审?[查阅]|批|显示|列出|给我).*的?作业")),
    END_CONTEXT(Regex("(OK|好|就这样|结束)了?"))
}

/**
 * Represent something that helps understand the commander's
 * order
 */
interface KnownInfo {
    val value: Any
}

class InClassroom(override val value: Long) : KnownInfo
abstract class HasAction(override val value: Action, val context: Conversation) : KnownInfo {
    abstract fun conclude(resolver: Conversation.ConclusionResolver): Conclusion?
}

class HasHomeworkAction(value: Action, context: Conversation, val dateRestriction: LocalDate, val subject: Subject) :
    HasAction(value, context) {
    override fun conclude(resolver: Conversation.ConclusionResolver): Conclusion? {
        val homeworks = context.clues.filterIsInstance<HasAttachment>()
        val identity = context.clues.lastOrNull { it is InClassroom }
        if (homeworks.isEmpty() || identity == null) {
            return null
        }
        resolver.requestEnd()
        val result = HomeworkManifest(subject, context.contact.id, dateRestriction)
        result.extra.addAll(homeworks)
        result.extra.add(identity)
        return result
    }
}

class HasEndAction(context: Conversation) : HasAction(Action.END_CONTEXT, context) {
    override fun conclude(resolver: Conversation.ConclusionResolver) = NothingToDo
}

class HasAttachment(override val value: Message) : KnownInfo

/**
 * Ambiguity that is needed to be resolved
 */
interface Uncertainty {
    val context: Conversation
    fun resolve()
}

class SubjectMappingAmbiguity(val subjects: List<Subject>, override val context: Conversation) : Uncertainty {

    private var timer: Timer? = null

    private var head = 0
    private suspend fun requestNext() {
        context.contact.sendMessage(subjects[head].names.first())
        head ++
        if (timer != null) {
            timer!!.cancel()
        }
        timer = fixedRateTimer(initialDelay = DURATION_AWAIT, period = DURATION_AWAIT) {
            suspend {
                requestNext()
            }
            cancel()
        }
    }

    private val listener = context.contact.bot.eventChannel.subscribe<MessageEvent> {
        ListeningStatus.LISTENING
    }

    @Deprecated("Not for production use")
    override fun resolve() {
        if (subjects.size <= 1) {
            error("Not ambiguous")
        }
        suspend {
            requestNext()
        }
        context.finally {
            listener.complete()
        }
    }

    companion object {
        const val DURATION_AWAIT = 2000L
    }
}

class Conversation(val contact: Contact) {
    private val endingActions = arrayListOf<(Conversation) -> Unit>()

    private var timer = startTimer()
    var ended: Boolean = false
        private set
    var conclusion: Conclusion? = null
        private set
    private var uncertaintyResolver: UncertaintyResolver? = null

    val clues = arrayListOf<KnownInfo>()
    val uncertainty = arrayListOf<Uncertainty>()

    fun process(nl: String): ProcessResult {
        if (uncertaintyResolver?.conversationBlocking == true) {
            return ProcessResult(false, listOf())
        }

        var matched = false
        val uncertainty = arrayListOf<Uncertainty>()
        Action.values().forEach { action ->
            val regex = action.regex
            val entire = regex.matchEntire(nl) ?: return@forEach
            matched = true
            if (action == Action.END_CONTEXT) {
                clues.add(HasEndAction(this))
            } else {
                val halves = regex.pattern.split(".*")
                if (halves.size != 2) {
                    error("Action ${action.name} not supported")
                }
                val date: LocalDate
                val left = Regex(halves[0])
                val right = Regex(halves[1])
                val phrase = entire.value.replace(constructors, "")
                val first = left.find(phrase)!!.range.last + 1
                val last = right.find(phrase)!!.range.first
                if (first >= last) {
                    throw SubjectNotSpecificException()
                } else {
                    var dateSubjectStr = phrase.substring(first, last)
                    val subjects = Subject.values().filter {
                        it.names.any { name ->
                            val origin = dateSubjectStr
                            dateSubjectStr = dateSubjectStr.replace(name, "")
                            dateSubjectStr != origin
                        }
                    }
                    if (subjects.size > 1) {
                        uncertainty.add(SubjectMappingAmbiguity(subjects, this))
                    }

                    date = if (dateSubjectStr.isEmpty() || keywordToday.matches(dateSubjectStr)) {
                        LocalDate.now(defaultZone)
                    } else if (keywordYesterday.matches(dateSubjectStr)) {
                        LocalDate.now(defaultZone).minusDays(1)
                    } else {
                        LocalDate.parse(dateSubjectStr, naturalDateFormat)
                    }

                    subjects.forEach {
                        clues.add(HasHomeworkAction(action, this, date, it))
                    }
                }

            }
        }
        this.uncertainty.addAll(uncertainty)
        val trial = conclude()
        if (trial != null) {
            conclusion = trial
            notifyConversationEnd()
        }
        return ProcessResult(matched, uncertainty)
    }

    fun process(image: Image) {
        clues.add(HasAttachment(image))
    }

    private fun conclude(): Conclusion? {
        if (!canEnd()) {
            return null
        }
        val resolver = ConclusionResolver()
        while (resolver.head < clues.size) {
            val knownInfo = clues[resolver.head]
            if (knownInfo is HasAction) {
                val trial = knownInfo.conclude(resolver)
                if (resolver.shouldEnd) {
                    if (trial == null) {
                        error("No enough info to conclude, but requested to")
                    }
                    return trial
                }
            }
            resolver.head ++
        }
        return null
    }

    fun canEnd(): Boolean {
        var identityKnown = false
        var goalKnown = false
        for (info in clues) {
            when (info) {
                is InClassroom -> identityKnown = true
                is HasEndAction -> return true
                is HasAction -> goalKnown = true
            }
            if (identityKnown && goalKnown) {
                return true
            }
        }
        return false
    }

    fun finally(action: (Conversation) -> Unit) {
        endingActions.add(action)
    }

    private fun startTimer() = fixedRateTimer(initialDelay = DURATION_AWAIT, period = DURATION_AWAIT) {
        if (!canEnd()) {
            return@fixedRateTimer
        }
        notifyConversationEnd()
    }

    private fun notifyConversationEnd() {
        ended = true
        endingActions.forEach {
            try {
                it.invoke(this)
            } catch (e: Exception) {
                Plugin.logger.warning("Failed to execute ${toString()}.endingActions: ${e.message}")
            }
        }
        timer.cancel()
    }

    companion object {
        const val DURATION_AWAIT = 30 * 1000L
        val keywordToday get() = Regex("今[天日]")
        val keywordYesterday get() = Regex("昨[天日]")
        val constructors get() = Regex("[的、,\\s]")
        val naturalDateFormat get() = DateTimeFormatter.ofPattern("M月d日").withZone(defaultZone)!!
    }

    class ConclusionResolver(var head: Int = 0) {
        var shouldEnd: Boolean = false
            private set
        fun requestEnd() {
            shouldEnd = true
        }
    }

    class UncertaintyResolver {
        internal var conversationBlocking: Boolean = false
            private set

        fun suspendConversation() {
            conversationBlocking = true
        }
    }
}

object NaturalLanguageProcessing {
    private val conversions = hashMapOf<Contact, Conversation>()
    private val endingActions = arrayListOf<(Conversation) -> Unit>()

    fun beginContext(contact: Contact, group: Group? = null): Conversation {
        if (conversions.contains(contact)) return conversions[contact]!!

        val conversation = Conversation(contact)
        conversation.finally {
            conversions.remove(contact)
        }
        if (group != null) {
            if (Database.classmates(group).contains(contact.id))
                conversation.clues.add(InClassroom(group.id))
        }
        conversions[contact] = conversation
        return conversation
    }

    fun onContextEnded(action: (Conversation) -> Unit) {
        endingActions.add(action)
    }
}

data class ProcessResult(val parsed: Boolean, val uncertainty: List<Uncertainty>)

class SubjectNotSpecificException : Exception("Subject not specific")
