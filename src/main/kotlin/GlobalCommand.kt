package com.zhufucdev

import com.zhufucdev.Query.Companion.buildNames
import com.zhufucdev.Query.Companion.tryQuery
import com.zhufucdev.data.Database
import com.zhufucdev.data.Database.isOp
import com.zhufucdev.data.SignUpRecord
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.getGroupOrNull
import net.mamoe.mirai.contact.*
import java.time.Instant

typealias MemberDelegate = (Member) -> Boolean
typealias MemberIdDelegate = (Long) -> Boolean

class Query(str: String, group: Group?) {
    class ContextNotFoundException : Exception()
    class NoSuchGroupException(val groupID: Long) : Exception()

    val targets = arrayListOf<Member>()
    val removal = arrayListOf<Member>()
    val basement: Group

    init {
        var split = str.split(',').map { it.trim() }
        basement = group ?: split.first().split('.')
            .let {
                fun reportError(): Nothing = throw ContextNotFoundException()

                if (it.size != 2) {
                    reportError()
                }
                val id = it.first().toLongOrNull() ?: reportError()
                for (bot in Bot.instances) {
                    val index = bot.getGroup(id)
                    if (index != null) {
                        split = split.drop(1).plus(it[1])
                        return@let index
                    }
                }
                throw NoSuchGroupException(id)
            }

        split.forEach { t ->
            if (t == "*") {
                targets.addAll(basement.members)
            } else if (t == "!*") {
                // exclude all
                targets.clear()
                removal.addAll(basement.members)
            } else {
                fun getMember(id: Long?) = if (id == null) {
                    val name = t.removePrefix("!").removePrefix("@")
                    if (name.startsWith(MIRAI_CODE_AT_PREFIX)) {
                        val id1 = name.removePrefix(MIRAI_CODE_AT_PREFIX).removeSuffix("]").toLongOrNull()
                        if (id1 == null)
                            null
                        else
                            basement.members.firstOrNull { it.id == id1 }
                    } else {
                        basement.members.firstOrNull { (it.nameCard.isNotEmpty() && it.nameCard == name) || it.nick == name }
                    }
                } else {
                    basement.getMember(id)
                }

                if (!t.startsWith('!')) {
                    val id = t.toLongOrNull()
                    targets.add(getMember(id) ?: return@forEach)
                } else {
                    // exclude mode
                    val id = t.removePrefix("!").toLongOrNull()
                    val member = getMember(id) ?: return@forEach
                    val removed = targets.remove(member)
                    if (!removed) {
                        removal.add(member)
                    }
                }
            }
        }
    }

    fun buildTargetList(include: MemberDelegate) = buildNames(targets, include)
    fun buildRemovalList(include: MemberDelegate) = buildNames(removal, include)

    companion object {
        fun buildNames(idList: List<Long>, include: MemberIdDelegate, nameGetter: (Long) -> String) =
            buildString {
                idList.forEach {
                    if (include(it)) {
                        append(nameGetter(it))
                        append(", ")
                    }
                }
                if (isNotEmpty()) {
                    deleteRange(length - 2, length)
                } else {
                    append('无')
                }
            }

        fun buildNames(members: List<Member>, include: MemberDelegate) =
            buildString {
                members.forEach {
                    if (include(it)) {
                        append(it.nameCardOrNick)
                        append(", ")
                    }
                }
                if (isNotEmpty()) {
                    deleteRange(length - 2, length)
                } else {
                    append('无')
                }
            }

        suspend fun CommandSender.tryQuery(selector: String, group: Group? = null, action: suspend Query.() -> Unit) {
            try {
                val query = Query(selector, group ?: getGroupOrNull())
                action(query)
            } catch (c: ContextNotFoundException) {
                sendMessage("参数错误: 在非群聊上下文中须指定目标群号，以\".\"为分隔")
            } catch (g: NoSuchGroupException) {
                sendMessage("在所有在线机器人的联系人中找不到群${g.groupID}")
            }
        }

        private const val MIRAI_CODE_AT_PREFIX = "[mirai:at"
    }
}

object GlobalCommand : CompositeCommand(
    Plugin, "class",
    description = "ClassBot指令集"
) {
    @SubCommand
    suspend fun CommandSender.op(selector: String) {
        tryQuery(selector) {
            if (!isOp(basement)) {
                return@tryQuery
            }
            if (targets.isNotEmpty()) {
                sendMessage("将${buildTargetList { Database.markAsAdmin(it) }}设置为机器人管理员")
            }
            if (removal.isNotEmpty()) {
                sendMessage("取消了${buildRemovalList { Database.undoAdmin(it) }}的管理员权限")
            }
            Database.sync()
        }
    }

    @SubCommand
    suspend fun CommandSender.classmate(selector: String) {
        tryQuery(selector) {
            if (targets.isNotEmpty()) {
                Database.markAsClassmates(targets)
                sendMessage("向群中新增了${targets.size}个学员")
            }
            if (removal.isNotEmpty()) {
                Database.removeClassmates(removal)
                sendMessage("从群中移除了${removal.size}个学员")
            }
            Database.sync()
        }
    }

    @SubCommand
    suspend fun CommandSender.signup(group: Group) {
        if (!isOp(group)) {
            return
        }
        val classmates = Database.getClassmates(group)
        sendMessage(
            "今日未签到者: ${
                buildNames(
                    classmates,
                    include = { !SignUpRecord.hasSignedToday(it, group) },
                    nameGetter = { group.getMember(it)?.nameCardOrNick ?: it.toString() })

            }"
        )
    }

    @SubCommand
    suspend fun MemberCommandSender.signup() {
        signup(group)
    }

    @SubCommand
    suspend fun CommandSender.sign(selector: String) {
        tryQuery(selector) {
            if (targets.isNotEmpty()) {
                sendMessage(
                    "将${
                        buildTargetList {
                            Database.record(
                                basement,
                                SignUpRecord(it.id, Instant.now())
                            ); true
                        }
                    }标记为已签"
                )
            }
            if (removal.isNotEmpty()) {
                sendMessage("将${
                    buildRemovalList { 
                        Database.unrecord(basement) { r ->
                            (r is SignUpRecord) && r.classmate == it.id
                        }
                    }
                }标记为未签")
            }
            Database.sync()
        }
    }
}