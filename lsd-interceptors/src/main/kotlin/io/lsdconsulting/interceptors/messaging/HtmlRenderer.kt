package io.lsdconsulting.interceptors.messaging

import j2html.TagCreator
import org.springframework.messaging.MessageHeaders

fun renderHtmlFor(messageHeaders: MessageHeaders, prettyBody: String?): String {
    return TagCreator.div(
        TagCreator.section(TagCreator.h3("Message Headers"), TagCreator.p(prettyPrintHeaders(messageHeaders))),
        if (prettyBody.isNullOrEmpty()) TagCreator.p()
        else TagCreator.section(
            TagCreator.h3("Body"),
            TagCreator.p(prettyBody)
        )
    ).render()
}

private fun prettyPrintHeaders(requestHeaders: MessageHeaders): String =
    requestHeaders.entries.joinToString(separator = System.lineSeparator()) { (key, value): Map.Entry<String, Any> ->
        "$key: $value"
    }
