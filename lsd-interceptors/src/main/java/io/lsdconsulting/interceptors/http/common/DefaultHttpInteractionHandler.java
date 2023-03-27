package io.lsdconsulting.interceptors.http.common;

import com.lsd.core.IdGenerator;
import com.lsd.core.LsdContext;
import com.lsd.core.builders.DeactivateLifelineBuilder;
import com.lsd.core.domain.ActivateLifeline;
import io.lsdconsulting.interceptors.common.Headers;
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings;
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

import static com.lsd.core.builders.ActivateLifelineBuilder.activation;
import static com.lsd.core.builders.DeactivateLifelineBuilder.*;
import static com.lsd.core.builders.MessageBuilder.messageBuilder;
import static com.lsd.core.domain.MessageType.SYNCHRONOUS;
import static com.lsd.core.domain.MessageType.SYNCHRONOUS_RESPONSE;
import static j2html.TagCreator.*;
import static java.lang.System.lineSeparator;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static lsd.format.PrettyPrinter.prettyPrint;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@ToString
@EqualsAndHashCode
public class DefaultHttpInteractionHandler implements HttpInteractionHandler {

    private final LsdContext lsdContext;
    private final SourceNameMappings sourceNameMappings;
    private final DestinationNameMappings destinationNameMappings;
    private final IdGenerator idGenerator;

    public DefaultHttpInteractionHandler(LsdContext lsdContext, SourceNameMappings sourceNameMappings, DestinationNameMappings destinationNameMappings) {
        this.lsdContext = lsdContext;
        this.sourceNameMappings = sourceNameMappings;
        this.destinationNameMappings = destinationNameMappings;
        this.idGenerator = lsdContext.getIdGenerator();
    }

    @Override
    public void handleRequest(String method, Map<String, String> requestHeaders, String path, String body) {
        var targetName = deriveTargetName(requestHeaders, path);
        lsdContext.capture(messageBuilder()
                .id(idGenerator.next())
                .from(deriveSourceName(requestHeaders, path))
                .to(targetName)
                .label(method + " " + path)
                .data(renderHtmlFor(path, requestHeaders, null, prettyPrint(body)))
                .type(SYNCHRONOUS)
                .build());
        lsdContext.capture(activation().of(targetName).colour("skyblue").build());
    }

    @Override
    public void handleResponse(String statusMessage, Map<String, String> requestHeaders, Map<String, String> responseHeaders, String path, String body) {
        String colour = "";
        if (statusMessage.startsWith("4") || statusMessage.startsWith("5")) colour = "red";

        var targetName = deriveTargetName(requestHeaders, path);
        lsdContext.capture(messageBuilder()
                .id(idGenerator.next())
                .from(targetName)
                .to(deriveSourceName(requestHeaders, path))
                .label(statusMessage)
                .data(renderHtmlFor(path, requestHeaders, null, prettyPrint(body)))
                .type(SYNCHRONOUS_RESPONSE)
                .colour(colour)
                .build());
        lsdContext.capture(deactivation().of(targetName).build());
    }

    private String renderHtmlFor(String path, Map<String, String> requestHeaders, Map<String, String> responseHeaders, String prettyBody) {
        return p(
                p(
                        h4("Full Path"),
                        span(path)
                ), isNull(responseHeaders)
                        ? p(h4("Request Headers"), code(prettyPrintHeaders(requestHeaders)))
                        : p(h4("Response Headers"), code(prettyPrintHeaders(responseHeaders)))
                , isEmpty(prettyBody)
                        ? p()
                        : p(h4("Body"), code(prettyBody)
                )
        ).render();
    }

    private String prettyPrintHeaders(Map<String, String> requestHeaders) {
        return requestHeaders.entrySet().stream().map(entry ->
                        entry.getKey() + ": " + entry.getValue())
                .collect(joining(lineSeparator()));
    }

    private String deriveTargetName(Map<String, String> headers, String path) {
        return headers.containsKey(Headers.HeaderKeys.TARGET_NAME.key())
                ? headers.get(Headers.HeaderKeys.TARGET_NAME.key())
                : destinationNameMappings.mapForPath(path);
    }

    private String deriveSourceName(Map<String, String> headers, String path) {
        return headers.containsKey(Headers.HeaderKeys.SOURCE_NAME.key())
                ? headers.get(Headers.HeaderKeys.SOURCE_NAME.key())
                : sourceNameMappings.mapForPath(path);
    }
}
