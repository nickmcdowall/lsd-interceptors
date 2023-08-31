package io.lsdconsulting.interceptors.rabbitmq

import lsd.format.prettyPrint
import org.springframework.amqp.core.Message

fun retrieve(message: Message): Map<String, Collection<String>> =
    message.messageProperties.headers.entries.associate {
        it.key to (it.value?.let { _ -> listOf(prettyPrint(it.value)) } ?: emptyList())
    }
