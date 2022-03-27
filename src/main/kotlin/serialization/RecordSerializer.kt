package com.zhufucdev.serialization

import com.google.gson.*
import com.zhufucdev.data.HomeworkRecord
import com.zhufucdev.data.Record
import com.zhufucdev.data.SignUpRecord
import com.zhufucdev.data.homework.Homework
import com.zhufucdev.data.homework.HomeworkManifest
import com.zhufucdev.data.homework.Revision
import com.zhufucdev.data.homework.Subject
import java.io.File
import java.lang.reflect.Type
import java.time.Instant
import java.time.LocalDate

class RecordSerializer : JsonSerializer<Record>, JsonDeserializer<Record> {

    private fun serializeCommon(src: Record, type: String) = JsonObject().apply {
        addProperty("mate", src.classmate)
        addProperty("time", src.timestamp.toString())
        addProperty("type", type)
    }

    override fun serialize(src: Record, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
        when (src) {
            is SignUpRecord -> serializeCommon(src, SignUpRecord.TYPE_STR)
            is HomeworkRecord -> serializeCommon(src, HomeworkRecord.TYPE_STR).apply {
                addProperty("subject", src.work.info.subject.name)
                addProperty("revision", src.work.revision.name)
                addProperty("date", src.work.info.date.toString())
                val files = JsonArray()
                src.work.files.forEach { files.add(it.toRelativeString(Homework.fileContainer)) }
                add("files", files)
            }
            else -> throw JsonParseException("${src::class.simpleName} is not supported to be serialized")
        }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Record {
        val obj = json.asJsonObject
        if (!obj.has("type")) {
            throw IllegalArgumentException("$json doesn't contain a type property")
        }
        return when (obj["type"].asString) {
            SignUpRecord.TYPE_STR -> SignUpRecord(obj["mate"].asLong, Instant.parse(obj["time"].asString))
            HomeworkRecord.TYPE_STR ->
                HomeworkRecord(
                    classmate = obj["mate"].asLong,
                    timestamp = Instant.parse(obj["time"].asString),
                    Homework(
                        info = HomeworkManifest(
                            student = obj["mate"].asLong,
                            subject = Subject.valueOf(obj["subject"].asString),
                            date = LocalDate.parse(obj["date"].asString)
                        ),
                        files = obj["files"].asJsonArray.map { File(Homework.fileContainer, it.asString) },
                        revision = Revision.valueOf(obj["revision"].asString)
                    )
                )
            else -> throw JsonParseException("type ${obj["type"].asString} is not supported to be deserialized")
        }
    }
}
