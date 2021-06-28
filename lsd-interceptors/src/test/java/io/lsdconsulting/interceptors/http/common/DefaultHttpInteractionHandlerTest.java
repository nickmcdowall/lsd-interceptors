package io.lsdconsulting.interceptors.http.common;

import com.lsd.LsdContext;
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings;
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

class DefaultHttpInteractionHandlerTest {

    private final Map<String, String> serviceNameHeaders = Map.of(
            Headers.HeaderKeys.TARGET_NAME.key(), "target",
            Headers.HeaderKeys.SOURCE_NAME.key(), "source"
    );
    private final SourceNameMappings sourceNameMapping = path -> "sourceName";
    private final DestinationNameMappings destinationNameMapping = path -> "destinationName";
    private final LsdContext lsdContext = Mockito.mock(LsdContext.class);

    private final DefaultHttpInteractionHandler handler = new DefaultHttpInteractionHandler(lsdContext, sourceNameMapping, destinationNameMapping);

    @Test
    void usesTestStateToLogRequest() {
        handler.handleRequest("GET", emptyMap(), "/path", "{\"type\":\"request\"}");

        verify(lsdContext).capture("GET /path from sourceName to destinationName",
                "<p>" +
                        "<p><h4>request path:</h4><sub>/path</sub></p>" +
                        "<p><h4>request headers:</h4><sub></sub></p>" +
                        "<p><h4>body:</h4><pre>{\n  &quot;type&quot;: &quot;request&quot;\n}</pre></p>" +
                        "</p>");
    }

    @Test
    void usesTestStateToLogResponse() {
        handler.handleResponse("200 OK", emptyMap(), "/path", "response body");

        verify(lsdContext).capture("sync 200 OK response from destinationName to sourceName",
                "<p>" +
                        "<p><h4>request path:</h4><sub>/path</sub></p>" +
                        "<p><h4>request headers:</h4><sub></sub></p>" +
                        "<p><h4>body:</h4><pre>response body</pre></p>" +
                        "</p>");
    }

    @Test
    void headerValuesForSourceAndDestinationArePreferredWhenLoggingRequest() {
        handler.handleRequest("GET", serviceNameHeaders, "/path", "");

        verify(lsdContext).capture(ArgumentMatchers.eq("GET /path from source to target"), anyString());
    }

    @Test
    void headerValuesForSourceAndDestinationArePreferredWhenLoggingResponse() {
        handler.handleResponse("200 OK", serviceNameHeaders, "/path", "response body");

        verify(lsdContext).capture(
                ArgumentMatchers.eq("sync 200 OK response from target to source"),
                ArgumentMatchers.contains("<pre>response body</pre>"));
    }
}