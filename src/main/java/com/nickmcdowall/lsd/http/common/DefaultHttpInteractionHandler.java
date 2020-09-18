package com.nickmcdowall.lsd.http.common;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.common.HttpInteractionHandler;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import lombok.EqualsAndHashCode;

import static com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates.requestOf;
import static com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates.responseOf;

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
    public void handleRequest(String method, String path, String body) {
        testState.log(requestOf(method, path, sourceNameMappings.mapForPath(path), destinationNameMappings.mapForPath(path)), body);
    }

    @Override
    public void handleResponse(String statusMessage, String path, String body) {
        testState.log(responseOf(statusMessage, destinationNameMappings.mapForPath(path), sourceNameMappings.mapForPath(path)), body);
    }
}
