package io.lsdconsulting.interceptors.rabbitmq

import j2html.TagCreator
import org.apache.commons.lang3.StringUtils

fun renderHtmlFor(messageHeaders: Map<String, Collection<String>>, prettyBody: String?): String {
    return TagCreator.div(
        TagCreator.section(TagCreator.h3("Message Headers"), TagCreator.p(prettyPrintHeaders(messageHeaders))),
        if (StringUtils.isEmpty(prettyBody)) TagCreator.p() else TagCreator.section(
            TagCreator.h3("Body"),
            TagCreator.p(prettyBody)
        )
    ).render()
}

private fun prettyPrintHeaders(requestHeaders: Map<String, Collection<String>>): String =
    requestHeaders.entries.joinToString(separator = System.lineSeparator()) { (key, value): Map.Entry<String, Any> -> "$key: $value" }
