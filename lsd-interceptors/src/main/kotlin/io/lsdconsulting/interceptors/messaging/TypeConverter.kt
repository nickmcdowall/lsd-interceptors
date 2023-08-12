package io.lsdconsulting.interceptors.messaging

import com.fasterxml.jackson.core.JsonProcessingException
import lsd.format.json.createObjectMapper

// TODO Replace with lsd-formatting-library
fun convertToString(payload: Any?): String =
    when (payload) {
        is String -> {
            payload
        }

        is ByteArray -> {
            String((payload as ByteArray?)!!)
        }

        else -> {
            try {
                createObjectMapper().writeValueAsString(payload)
            } catch (e: JsonProcessingException) {
                throw RuntimeException(e)
            }
        }
    }
