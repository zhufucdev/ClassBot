package com.zhufucdev

import com.zhufucdev.data.homework.HomeworkManifest
import com.zhufucdev.data.homework.Subject
import com.zhufucdev.serialization.defaultZone
import net.mamoe.mirai.message.data.PlainText
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout
import java.time.Duration
import java.time.LocalDate
import kotlin.test.*

class NLPTest {
    val conversation = NaturalLanguageProcessing.beginContext(FakeContact(233))

    fun summon() {
        assertTrue("Not detected") {
            conversation.process("提交今天的数学作业").parsed
        }
        assertTrue("Not detected") {
            conversation.process("修改昨天的语文作业").parsed
        }
        assertTrue("Not detected") {
            conversation.process("查地理作业").parsed
        }
        assertTrue("Not detected") {
            conversation.process("好了").parsed
        }
    }

    @Test
    fun testGoalDetection() {
        var isEnded = false
        conversation.finally {
            isEnded = true
        }
        summon()

        val parse = conversation.clues[0]
        val parse2 = conversation.clues[1]
        val parse3 = conversation.clues[2]
        assertSame(parse.value, Action.HAND_IN_HOMEWORK, "Type not match")
        assertSame(parse2.value, Action.AMEND_HOMEWORK, "Type not match")
        assertSame(parse3.value, Action.REVIEW_HOMEWORK, "Type not match")
        assertTimeout(Duration.ofSeconds(1)) { isEnded }
    }

    @Test
    fun testDateDetection() {
        summon()

        val today = LocalDate.now(defaultZone)

        val parse = conversation.clues.first()
        assertIs<HasHomeworkAction>(parse)
        assertTrue(parse.dateRestriction.isEqual(today), "Declarative date not match")

        val parse2 = conversation.clues[1]
        assertIs<HasHomeworkAction>(parse2)
        assertTrue(parse2.dateRestriction.isEqual(today.minusDays(1)), "Declarative date not match")

        val parse3 = conversation.clues[2]
        assertIs<HasHomeworkAction>(parse3)
        assertTrue(parse3.dateRestriction.isEqual(today), "Indeclarative date not match")
    }

    private fun summon(following: Boolean) {
        val context = NaturalLanguageProcessing.beginContext(FakeContact(233))
        if (!following) {
            context.process("提交数学作业")
        }
        context.clues.add(HasAttachment(PlainText("test")))
        context.clues.add(InClassroom(2333))
        if (following) {
            context.process("提交数学作业")
        } else {
            context.process("好了")
        }

        val conclusion = context.conclusion
        assertNotNull(conclusion, "No conclusion")
        assertIs<HomeworkManifest>(conclusion, "Not detected")
        assertEquals(233, conclusion.student, "Student is wrong")
        assertEquals(
            2333,
            conclusion.extra.filterIsInstance<InClassroom>().firstOrNull()?.value,
            "Classroom is wrong"
        )
        assertSame(Subject.MATHEMATICS, conclusion.subject, "Subject is wrong")
    }

    /**
     * **Following Action** is an action that comes after
     * attachments
     */
    @Test
    fun testConcludingFollowing() {
        summon(true)
    }

    @Test
    fun testConcludingFollowed() {
        summon(false)
    }
}