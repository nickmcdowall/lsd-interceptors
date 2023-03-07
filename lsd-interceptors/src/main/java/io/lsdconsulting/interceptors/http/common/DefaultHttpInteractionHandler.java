package io.lsdconsulting.interceptors.http.common;

import com.lsd.LsdContext;
import com.lsd.diagram.ValidComponentName;
import com.lsd.events.Markup;
import io.lsdconsulting.interceptors.common.Headers;
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings;
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

import static io.lsdconsulting.interceptors.http.common.HttpInteractionMessageTemplates.requestOf;
import static io.lsdconsulting.interceptors.http.common.HttpInteractionMessageTemplates.responseOf;
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

    public DefaultHttpInteractionHandler(LsdContext lsdContext, SourceNameMappings sourceNameMappings, DestinationNameMappings destinationNameMappings) {
        this.lsdContext = lsdContext;
        this.sourceNameMappings = sourceNameMappings;
        this.destinationNameMappings = destinationNameMappings;
    }

    @Override
    public void handleRequest(String method, Map<String, String> requestHeaders, String path, String body) {
        String sourceName = ValidComponentName.of(deriveSourceName(requestHeaders, path));
        String destinationName = ValidComponentName.of(deriveTargetName(requestHeaders, path));
        lsdContext.capture(requestOf(method, path, sourceName, destinationName), renderHtmlFor(path, requestHeaders, null, prettyPrint(body)));
        lsdContext.capture(new Markup("activate " + destinationName + "#skyblue"));
    }

    @Override
    public void handleResponse(String statusMessage, Map<String, String> requestHeaders, Map<String, String> responseHeaders, String path, String body) {
        String destinationName = ValidComponentName.of(deriveTargetName(requestHeaders, path));
        String sourceName = ValidComponentName.of(deriveSourceName(requestHeaders, path));
        lsdContext.capture(responseOf(statusMessage, destinationName, sourceName), renderHtmlFor(path, requestHeaders, responseHeaders, prettyPrint(body)));
        lsdContext.capture(new Markup("deactivate " + destinationName));
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
