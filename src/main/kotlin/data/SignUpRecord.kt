package com.zhufucdev.data

import java.time.Instant

class SignUpRecord(classmate: Long, timestamp: Instant) : Record(classmate, timestamp) {
    companion object {
        const val TYPE_STR = "signup"
    }
}
