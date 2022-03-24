package com.zhufucdev.data

import com.zhufucdev.data.homework.Subject
import java.time.Instant

class HomeworkRecord(classmate: Long, timestamp: Instant, val subject: Subject) : Record(classmate, timestamp) {

}