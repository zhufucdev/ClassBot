package com.zhufucdev.data.homework

import com.zhufucdev.Conclusion
import java.time.LocalDate

/**
 * Manifest to build a [Homework] instance
 */
data class HomeworkManifest(val subject: Subject, val student: Long, val date: LocalDate) : Conclusion() {
    fun create() = Homework(this, arrayListOf(), Revision.NOT_HANDED)
}
