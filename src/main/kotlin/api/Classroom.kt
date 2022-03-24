package com.zhufucdev.api

import com.zhufucdev.Plugin
import com.zhufucdev.Query

@kotlinx.serialization.Serializable
data class Classroom(val id: Long, val name: String) {
    companion object {
        fun from(id: Long): Classroom {
            val group = Plugin.getGroup(id) ?: throw Query.NoSuchGroupException(id)
            return Classroom(id = group.id, name = group.name)
        }
    }
}