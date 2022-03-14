package com.zhufucdev.data

import java.time.Instant

abstract class Record(
    val classmate: Long,
    val timestamp: Instant
) {
    override fun equals(other: Any?): Boolean =
        other is Record && other.classmate == this.classmate
            && other.timestamp == this.timestamp

    override fun hashCode(): Int {
        var result = classmate.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}