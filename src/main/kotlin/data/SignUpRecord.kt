package com.zhufucdev.data

import com.zhufucdev.serialization.defaultZone
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import java.time.Instant
import java.time.LocalDateTime

class SignUpRecord(classmate: Long, timestamp: Instant) : Record(classmate, timestamp) {
    companion object {
        const val TYPE_STR = "signup"

        fun hasSignedToday(classmate: Long, group: Group) = getTodayRecord(classmate, group) != null

        fun getTodayRecord(classmate: Long, group: Group) = Database[group].firstOrNull {
            it is SignUpRecord && it.classmate == classmate && LocalDateTime.ofInstant(it.timestamp, defaultZone)
                .toLocalDate().isEqual(LocalDateTime.now().atZone(defaultZone).toLocalDate())
        }
    }
}
