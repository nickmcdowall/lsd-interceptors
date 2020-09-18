package com.nickmcdowall.lsd.http.common;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;

class DefaultHttpInteractionHandlerTest {

    private final SourceNameMappings sourceNameMapping = path -> "sourceName";
    private final DestinationNameMappings destinationNameMapping = path -> "destinationName";
    private final TestState testState = Mockito.mock(TestState.class);

    private final DefaultHttpInteractionHandler handler = new DefaultHttpInteractionHandler(testState, sourceNameMapping, destinationNameMapping);

    @Test
    void usesTestStateToLogRequest() {
        handler.handleRequest("GET", "/path", "body");

        verify(testState).log("GET /path from sourceName to destinationName", "body");
    }

    @Test
    void usesTestStateToLogResponse() {
        handler.handleResponse("200 OK", "/path", "response body");

        verify(testState).log("200 OK response from destinationName to sourceName", "response body");
    }
}