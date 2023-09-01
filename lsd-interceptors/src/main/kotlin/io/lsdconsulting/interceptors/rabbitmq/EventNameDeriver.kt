package io.lsdconsulting.interceptors.rabbitmq

import org.springframework.amqp.core.MessageProperties

fun deriveEventName(messageProperties: MessageProperties, alternativeEventName: String?): String =
    deriveFromTypeIdHeader(messageProperties.getHeader(TYPE_ID_HEADER))
        ?: getDefaultExchangeName(alternativeEventName)

private fun getDefaultExchangeName(alternativeExchangeName: String?): String =
    if (!alternativeExchangeName.isNullOrBlank()) alternativeExchangeName else UNKNOWN_EVENT

private fun deriveFromTypeIdHeader(typeIdHeader: String?): String? =
    typeIdHeader?.split("\\.".toRegex())?.reduce { _: String?, second: String -> second }?.ifBlank { null }

private const val TYPE_ID_HEADER = "__TypeId__"
private const val UNKNOWN_EVENT = "UNKNOWN_EVENT"
