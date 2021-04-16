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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

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

        verify(testState).log("GET /path from sourceName to destinationName",
                "<section>\n" +
                        "    <details>\n" +
                        "        <summary>\n" +
                        "            path\n" +
                        "        </summary>\n" +
                        "        <section>\n" +
                        "            <pre>/path</pre>\n" +
                        "        </section>\n" +
                        "    </details>\n" +
                        "    <details>\n" +
                        "        <summary>\n" +
                        "            requestHeaders\n" +
                        "        </summary>\n" +
                        "        <section>\n" +
                        "            <pre>{}</pre>\n" +
                        "        </section>\n" +
                        "    </details>\n" +
                        "    <section>\n" +
                        "        <pre>{\n" +
                        "  &quot;type&quot;: &quot;request&quot;\n" +
                        "}</pre>\n" +
                        "    </section>\n" +
                        "</section>\n");
    }

    @Test
    void usesTestStateToLogResponse() {
        handler.handleResponse("200 OK", emptyMap(), "/path", "response body");

        verify(testState).log("200 OK response from destinationName to sourceName",
                "<section>\n" +
                        "    <details>\n" +
                        "        <summary>\n" +
                        "            path\n" +
                        "        </summary>\n" +
                        "        <section>\n" +
                        "            <pre>/path</pre>\n" +
                        "        </section>\n" +
                        "    </details>\n" +
                        "    <details>\n" +
                        "        <summary>\n" +
                        "            requestHeaders\n" +
                        "        </summary>\n" +
                        "        <section>\n" +
                        "            <pre>{}</pre>\n" +
                        "        </section>\n" +
                        "    </details>\n" +
                        "    <section>\n" +
                        "        <pre>response body</pre>\n" +
                        "    </section>\n" +
                        "</section>\n");
    }

    @Test
    void headerValuesForSourceAndDestinationArePreferredWhenLoggingRequest() {
        handler.handleRequest("GET", serviceNameHeaders, "/path", "");

        verify(testState).log(argThat(equalTo("GET /path from source to target")), anyString());
    }

    @Test
    void headerValuesForSourceAndDestinationArePreferredWhenLoggingResponse() {
        handler.handleResponse("200 OK", serviceNameHeaders, "/path", "response body");

        verify(testState).log(
                argThat(equalTo("200 OK response from target to source")),
                argThat(containsString("<pre>response body</pre>")));
    }
}