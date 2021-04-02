package no.uio.in2000.team16.flynerd.util

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal fun GsonBuilder.registerLocalDateTime() = registerTypeAdapter(
    LocalDateTime::class.java,
    object : JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext,
        ) = LocalDateTime.parse(
            json.asJsonPrimitive.asString,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )

        override fun serialize(
            src: LocalDateTime,
            typeOfSrc: Type,
            context: JsonSerializationContext,
        ) = JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }
)

internal fun GsonBuilder.registerZonedDateTime() = registerTypeAdapter(
    ZonedDateTime::class.java,
    object : JsonDeserializer<ZonedDateTime>, JsonSerializer<ZonedDateTime> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext,
        ) = ZonedDateTime.parse(
            json.asJsonPrimitive.asString,
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
        )

        override fun serialize(
            src: ZonedDateTime,
            typeOfSrc: Type,
            context: JsonSerializationContext,
        ) = JsonPrimitive(src.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
    }
)
