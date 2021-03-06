package org.http4k.format

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.math.BigDecimal
import java.math.BigInteger

open class ConfigurableGson(builder: GsonBuilder) : Json<JsonElement, JsonElement> {

    override fun typeOf(value: JsonElement): JsonType =
        if (value.isJsonArray) JsonType.Array
        else if (value.isJsonNull) JsonType.Null
        else if (value.isJsonObject) JsonType.Object
        else if (value.isJsonPrimitive) {
            val prim = value.asJsonPrimitive
            if (prim.isBoolean) JsonType.Boolean
            else if (prim.isNumber) JsonType.Number
            else if (prim.isString) JsonType.String
            else throw IllegalArgumentException("Don't know now to translate $value")
        } else throw IllegalArgumentException("Don't know now to translate $value")

    private val compact = builder.create()
    private val pretty = builder.setPrettyPrinting().create()

    override fun String.asJsonObject(): JsonElement = JsonParser().parse(this).asJsonObject
    override fun String?.asJsonValue(): JsonElement = this?.let { JsonPrimitive(this) } ?: JsonNull.INSTANCE
    override fun Int?.asJsonValue(): JsonElement = this?.let { JsonPrimitive(this) } ?: JsonNull.INSTANCE
    override fun Double?.asJsonValue(): JsonElement = this?.let { JsonPrimitive(BigDecimal(this)) } ?: JsonNull.INSTANCE
    override fun Long?.asJsonValue(): JsonElement = this?.let { JsonPrimitive(this) } ?: JsonNull.INSTANCE
    override fun BigDecimal?.asJsonValue(): JsonElement = this?.let { JsonPrimitive(this) } ?: JsonNull.INSTANCE
    override fun BigInteger?.asJsonValue(): JsonElement = this?.let { JsonPrimitive(this) } ?: JsonNull.INSTANCE
    override fun Boolean?.asJsonValue(): JsonElement = this?.let { JsonPrimitive(this) } ?: JsonNull.INSTANCE
    override fun <T : Iterable<JsonElement>> T.asJsonArray(): JsonElement = this.fold(JsonArray()) { memo, o -> memo.add(o); memo }

    override fun JsonElement.asPrettyJsonString(): String = pretty.toJson(this)
    override fun JsonElement.asCompactJsonString(): String = compact.toJson(this)
    override fun <LIST : Iterable<Pair<String, JsonElement>>> LIST.asJsonObject(): JsonElement {
        val root = JsonObject()
        this.forEach { root.add(it.first, it.second) }
        return root
    }

    override fun fields(node: JsonElement): Iterable<Pair<String, JsonElement>> {
        val fieldList = mutableListOf<Pair<String, JsonElement>>()
        for ((key, value) in node.asJsonObject.entrySet()) {
            fieldList += key to value
        }
        return fieldList
    }

    override fun elements(value: JsonElement): Iterable<JsonElement> = value.asJsonArray
    override fun text(value: JsonElement): String = value.asString

}

object Gson : ConfigurableGson(GsonBuilder().serializeNulls())
