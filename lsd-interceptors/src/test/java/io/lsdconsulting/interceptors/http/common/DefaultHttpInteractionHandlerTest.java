package io.lsdconsulting.interceptors.http.common;

import com.lsd.core.LsdContext;
import com.lsd.core.domain.Message;
import com.lsd.core.domain.MessageType;
import com.lsd.core.domain.SequenceEvent;
import io.lsdconsulting.interceptors.common.Headers;
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings;
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DefaultHttpInteractionHandlerTest {

    private final Map<String, String> serviceNameHeaders = Map.of(
            Headers.HeaderKeys.TARGET_NAME.key(), "target",
            Headers.HeaderKeys.SOURCE_NAME.key(), "source"
    );
    private final SourceNameMappings sourceNameMapping = path -> "sourceName";
    private final DestinationNameMappings destinationNameMapping = path -> "destinationName";

    private final ArgumentCaptor<SequenceEvent> messageCaptor = ArgumentCaptor.forClass(SequenceEvent.class);

    private final LsdContext lsdContext = Mockito.spy(LsdContext.class);

    private final DefaultHttpInteractionHandler handler = new DefaultHttpInteractionHandler(lsdContext, sourceNameMapping, destinationNameMapping);

    @Test
    void usesTestStateToLogRequest() {
        handler.handleRequest("GET", emptyMap(), "/path", "{\"type\":\"request\"}");

        verify(lsdContext, times(2)).capture(messageCaptor.capture());
        var message = extractFirstMessageFromCaptor();

        assertThat(message.getFrom().getName()).isEqualTo("SourceName");
        assertThat(message.getTo().getName()).isEqualTo("DestinationName");
        assertThat(message.getLabel()).isEqualTo("GET /path");
        assertThat(message.getType()).isEqualTo(MessageType.SYNCHRONOUS);
        assertThat(message.getData().toString())
                .contains("<code>{\n  &quot;type&quot;: &quot;request&quot;\n}</code>");
    }

    @Test
    void usesTestStateToLogResponse() {
        handler.handleResponse("200 OK", emptyMap(), emptyMap(), "/path", "response body");

        verify(lsdContext, times(2)).capture(messageCaptor.capture());
        var message = extractFirstMessageFromCaptor();

        assertThat(message.getFrom().getName()).isEqualTo("DestinationName");
        assertThat(message.getTo().getName()).isEqualTo("SourceName");
        assertThat(message.getLabel()).isEqualTo("200 OK");
        assertThat(message.getType()).isEqualTo(MessageType.SYNCHRONOUS_RESPONSE);
        assertThat(message.getData().toString())
                .contains("<code>response body</code");
    }

    @Test
    void headerValuesForSourceAndDestinationArePreferredWhenLoggingRequest() {
        handler.handleRequest("GET", serviceNameHeaders, "/path", "");

        verify(lsdContext, times(2)).capture(messageCaptor.capture());
        var message = extractFirstMessageFromCaptor();

        assertThat(message.getFrom().getName()).isEqualTo("Source");
        assertThat(message.getTo().getName()).isEqualTo("Target");
        assertThat(message.getLabel()).isEqualTo("GET /path");
        assertThat(message.getType()).isEqualTo(MessageType.SYNCHRONOUS);
        assertThat(message.getData().toString())
                .contains("Source-Name: source")
                .contains("Target-Name: target");
    }

    @NotNull
    private Message extractFirstMessageFromCaptor() {
        return messageCaptor.getAllValues().stream()
                .filter(Message.class::isInstance)
                .map(Message.class::cast)
                .findFirst().orElseThrow();
    }

    @Test
    void headerValuesForSourceAndDestinationArePreferredWhenLoggingResponse() {
        handler.handleResponse("200 OK", serviceNameHeaders, emptyMap(), "/path", "response body");

        verify(lsdContext, times(2)).capture(messageCaptor.capture());
        var message = extractFirstMessageFromCaptor();

        assertThat(message.getFrom().getName()).isEqualTo("Target");
        assertThat(message.getTo().getName()).isEqualTo("Source");
        assertThat(message.getLabel()).isEqualTo("200 OK");
        assertThat(message.getType()).isEqualTo(MessageType.SYNCHRONOUS_RESPONSE);
        assertThat(message.getData().toString())
                .contains("<h4>Request Headers</h4>")
                .contains("Target-Name: target")
                .contains("Source-Name: source")
                .contains("<code>response body</code>");
    }
}