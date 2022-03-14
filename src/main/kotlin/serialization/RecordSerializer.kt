package com.zhufucdev.serialization

import com.google.gson.*
import com.zhufucdev.data.Record
import com.zhufucdev.data.SignUpRecord
import java.lang.reflect.Type
import java.time.Instant

class RecordSerializer : JsonSerializer<Record>, JsonDeserializer<Record> {

    private fun serializeCommon(src: Record, type: String) = JsonObject().apply {
        addProperty("mate", src.classmate)
        addProperty("time", src.timestamp.toString())
        addProperty("type", type)
    }

    override fun serialize(src: Record, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
        when (src) {
            is SignUpRecord -> serializeCommon(src, SignUpRecord.TYPE_STR)
            else -> throw JsonParseException("${src::class.simpleName} is not supported to be serialized")
        }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Record {
        val obj = json.asJsonObject
        if (!obj.has("type")) {
            throw IllegalArgumentException("$json doesn't contain a type property")
        }
        return when (obj["type"].asString) {
            SignUpRecord.TYPE_STR -> SignUpRecord(obj["mate"].asLong, Instant.parse(obj["time"].asString))
            else -> throw JsonParseException("type ${obj["type"].asString} is not supported to be deserialized")
        }
    }
}
