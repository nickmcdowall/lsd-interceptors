package com.nickmcdowall.lsd.http.common;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import lombok.EqualsAndHashCode;

import java.util.Map;

import static com.nickmcdowall.lsd.http.common.Headers.HeaderKeys.SOURCE_NAME;
import static com.nickmcdowall.lsd.http.common.Headers.HeaderKeys.TARGET_NAME;
import static com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates.requestOf;
import static com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates.responseOf;
import static com.nickmcdowall.lsd.http.common.PrettyPrinter.prettyPrint;
import static j2html.TagCreator.*;

@EqualsAndHashCode
public class DefaultHttpInteractionHandler implements HttpInteractionHandler {

    private final TestState testState;
    private final SourceNameMappings sourceNameMappings;
    private final DestinationNameMappings destinationNameMappings;

    public DefaultHttpInteractionHandler(TestState testState, SourceNameMappings sourceNameMappings, DestinationNameMappings destinationNameMappings) {
        this.testState = testState;
        this.sourceNameMappings = sourceNameMappings;
        this.destinationNameMappings = destinationNameMappings;
    }

    @Override
    public void handleRequest(String method, Map<String, String> requestHeaders, String path, String body) {
        String sourceName = deriveSourceName(requestHeaders, path);
        String destinationName = deriveTargetName(requestHeaders, path);
        testState.log(requestOf(method, path, sourceName, destinationName), renderHtmlFor(path, requestHeaders, prettyPrint(body)));
    }

    @Override
    public void handleResponse(String statusMessage, Map<String, String> requestHeaders, String path, String body) {
        String destinationName = deriveTargetName(requestHeaders, path);
        String sourceName = deriveSourceName(requestHeaders, path);
        testState.log(responseOf(statusMessage, destinationName, sourceName), renderHtmlFor(path, requestHeaders, prettyPrint(body)));
    }

    private String renderHtmlFor(String path, Map<String, String> requestHeaders, String prettyBody) {
        var popupValue = section(
                details(
                        summary("path"),
                        section(pre(path))
                ), details(
                        summary("requestHeaders"),
                        section(pre(requestHeaders.toString()))
                ),
                section(pre(prettyBody))
        ).renderFormatted();
        return popupValue;
    }

    private String deriveTargetName(Map<String, String> headers, String path) {
        return headers.containsKey(TARGET_NAME.key())
                ? headers.get(TARGET_NAME.key())
                : destinationNameMappings.mapForPath(path);
    }

    private String deriveSourceName(Map<String, String> headers, String path) {
        return headers.containsKey(SOURCE_NAME.key())
                ? headers.get(SOURCE_NAME.key())
                : sourceNameMappings.mapForPath(path);
    }
}
