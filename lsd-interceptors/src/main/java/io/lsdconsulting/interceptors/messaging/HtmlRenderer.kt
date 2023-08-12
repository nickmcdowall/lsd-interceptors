package io.lsdconsulting.interceptors.messaging

import j2html.TagCreator
import org.apache.commons.lang3.StringUtils
import org.springframework.messaging.MessageHeaders
import java.util.stream.Collectors

fun renderHtmlFor(messageHeaders: MessageHeaders, prettyBody: String?): String {
    return TagCreator.div(
        TagCreator.section(TagCreator.h3("Message Headers"), TagCreator.p(prettyPrintHeaders(messageHeaders))),
        if (StringUtils.isEmpty(prettyBody)) TagCreator.p() else TagCreator.section(
            TagCreator.h3("Body"),
            TagCreator.p(prettyBody)
        )
    ).render()
}

private fun prettyPrintHeaders(requestHeaders: MessageHeaders): String {
    return requestHeaders.entries.stream().map { (key, value): Map.Entry<String, Any> -> "$key: $value" }
        .collect(Collectors.joining(System.lineSeparator()))
}
