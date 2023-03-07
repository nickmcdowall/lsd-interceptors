package io.lsdconsulting.interceptors.messaging;

import org.springframework.messaging.MessageHeaders;

import static j2html.TagCreator.*;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class HtmlRenderer {
    public static String renderHtmlFor(MessageHeaders messageHeaders, String prettyBody) {
        return p(
                p(h4("Message Headers"), code(prettyPrintHeaders(messageHeaders)))
                , isEmpty(prettyBody)
                        ? p()
                        : p(h4("Body"), code(prettyBody)
                )
        ).render();
    }

    private static String prettyPrintHeaders(MessageHeaders requestHeaders) {
        return requestHeaders.entrySet().stream().map(entry ->
                        entry.getKey() + ": " + entry.getValue())
                .collect(joining(lineSeparator()));
    }
}
