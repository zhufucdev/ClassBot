package com.zhufucdev.data

import com.zhufucdev.data.homework.Subject

data class Configuration(
    val admins: ArrayList<Long> = arrayListOf(),
    val classmates: ArrayList<Long> = arrayListOf(),
    val subjectPrincipal: ArrayList<Subject> = arrayListOf()
)
