package com.zhufucdev.api

import com.zhufucdev.Plugin
import com.zhufucdev.Query
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.nameCardOrNick

@kotlinx.serialization.Serializable
data class Student(val id: Long, val name: String) {
    companion object {
        fun from(id: Long, classroom: Long): Student {
            val group = Plugin.getGroup(classroom) ?: throw Query.NoSuchGroupException(classroom)
            val name = group.getMember(id)?.nameCardOrNick ?: error("No such member")
            return Student(id, name)
        }
    }
}
