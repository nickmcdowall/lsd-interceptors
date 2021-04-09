package com.nickmcdowall.lsd.http.common;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static com.nickmcdowall.lsd.http.common.Headers.HeaderKeys.SOURCE_NAME;
import static com.nickmcdowall.lsd.http.common.Headers.HeaderKeys.TARGET_NAME;
import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.verify;

class DefaultHttpInteractionHandlerTest {

    private final Map<String, String> serviceNameHeaders = Map.of(
            TARGET_NAME.key(), "target",
            SOURCE_NAME.key(), "source"
    );
    private final SourceNameMappings sourceNameMapping = path -> "sourceName";
    private final DestinationNameMappings destinationNameMapping = path -> "destinationName";
    private final TestState testState = Mockito.mock(TestState.class);

    private final DefaultHttpInteractionHandler handler = new DefaultHttpInteractionHandler(testState, sourceNameMapping, destinationNameMapping);

    @Test
    void usesTestStateToLogRequest() {
        handler.handleRequest("GET", emptyMap(), "/path", "{\"type\":\"request\"}");

        verify(testState).log("GET /path from sourceName to destinationName", "{\n  \"type\": \"request\"\n}");
    }

    @Test
    void usesTestStateToLogResponse() {
        handler.handleResponse("200 OK", emptyMap(), "/path", "response body");

        verify(testState).log("200 OK response from destinationName to sourceName", "response body");
    }

    @Test
    void headerValuesForSourceAndDestinationArePreferredWhenLoggingRequest() {
        handler.handleRequest("GET", serviceNameHeaders, "/path", "");

        verify(testState).log("GET /path from source to target", "");
    }

    @Test
    void headerValuesForSourceAndDestinationArePreferredWhenLoggingResponse() {
        handler.handleResponse("200 OK", serviceNameHeaders, "/path", "response body");

        verify(testState).log("200 OK response from target to source", "response body");
    }
}