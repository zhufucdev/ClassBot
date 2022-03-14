package com.zhufucdev.serialization

import com.google.gson.GsonBuilder
import com.zhufucdev.data.Record

val gson =
    GsonBuilder()
        .registerTypeAdapter(Record::class.java, RecordSerializer())
        .setPrettyPrinting()
        .create()