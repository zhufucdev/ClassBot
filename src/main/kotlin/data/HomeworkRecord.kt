package com.zhufucdev.data

import com.zhufucdev.data.homework.Homework
import java.time.Instant

class HomeworkRecord(classmate: Long, timestamp: Instant, val work: Homework) : Record(classmate, timestamp) {
    companion object {
        const val TYPE_STR = "homework"
    }
}