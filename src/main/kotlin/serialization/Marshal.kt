package com.zhufucdev.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zhufucdev.data.Record
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

val defaultZone: ZoneId = ZoneId.of("Asia/Shanghai")

val gson: Gson =
    GsonBuilder()
        .registerTypeAdapter(Record::class.java, RecordSerializer())
        .setPrettyPrinting()
        .create()

val dateFormat: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.CHINA).withZone(defaultZone)
