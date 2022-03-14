package com.zhufucdev.serialization

import com.google.gson.GsonBuilder
import com.zhufucdev.data.Record
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

val gson =
    GsonBuilder()
        .registerTypeAdapter(Record::class.java, RecordSerializer())
        .setPrettyPrinting()
        .create()

val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.CHINA).withZone(ZoneId.systemDefault())
