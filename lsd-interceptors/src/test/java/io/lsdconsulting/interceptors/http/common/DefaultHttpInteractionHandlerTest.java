package io.lsdconsulting.interceptors.http.common;

import com.lsd.core.LsdContext;
import com.lsd.core.domain.Message;
import com.lsd.core.domain.MessageType;
import com.lsd.core.domain.Participant;
import com.lsd.core.domain.SequenceEvent;
import io.lsdconsulting.interceptors.common.HeaderKeys;
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings;
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Map;

import static com.lsd.core.domain.ParticipantType.PARTICIPANT;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DefaultHttpInteractionHandlerTest {

    private final Map<String, String> serviceNameHeaders = Map.of(
            HeaderKeys.TARGET_NAME.key(), "bob",
            HeaderKeys.SOURCE_NAME.key(), "juliet"
    );
    private final SourceNameMappings sourceNameMapping = path -> "andrea";
    private final DestinationNameMappings destinationNameMapping = path -> "bren";

    private final ArgumentCaptor<SequenceEvent> messageCaptor = ArgumentCaptor.forClass(SequenceEvent.class);

    private final LsdContext lsdContext = Mockito.spy(LsdContext.class);

    private final DefaultHttpInteractionHandler handler = new DefaultHttpInteractionHandler(lsdContext, sourceNameMapping, destinationNameMapping);
    private final Participant bob = PARTICIPANT.called("bob");
    private final Participant juliet = PARTICIPANT.called("juliet");
    private final Participant bren = PARTICIPANT.called("bren");
    private final Participant andrea = PARTICIPANT.called("andrea");

    @Test
    void usesTestStateToLogRequest() {
        handler.handleRequest("GET", emptyMap(), "/path", "{\"type\":\"request\"}");

        verify(lsdContext, times(1)).capture(messageCaptor.capture());
        var message = extractFirstMessageFromCaptor();

        assertThat(message.getFrom()).isEqualTo(andrea);
        assertThat(message.getTo()).isEqualTo(bren);
        assertThat(message.getLabel()).isEqualTo("GET /path");
        assertThat(message.getType()).isEqualTo(MessageType.SYNCHRONOUS);
        assertThat(message.getData().toString())
                .contains("<p>{\n  &quot;type&quot;: &quot;request&quot;\n}</p>");
    }

    @Test
    void usesTestStateToLogResponse() {
        handler.handleResponse("200 OK", emptyMap(), emptyMap(), "/path", "response body", Duration.ofMillis(5));

        verify(lsdContext, times(1)).capture(messageCaptor.capture());
        var message = extractFirstMessageFromCaptor();

        assertThat(message.getFrom()).isEqualTo(bren);
        assertThat(message.getTo()).isEqualTo(andrea);
        assertThat(message.getLabel()).isEqualTo("200 OK (5ms)");
        assertThat(message.getType()).isEqualTo(MessageType.SYNCHRONOUS_RESPONSE);
        assertThat(message.getData().toString()).contains("<p>response body</p");
        assertThat(message.getDuration()).isEqualTo(Duration.ofMillis(5));
    }

    @Test
    void headerValuesForSourceAndDestinationArePreferredWhenLoggingRequest() {
        handler.handleRequest("GET", serviceNameHeaders, "/path", "");

        verify(lsdContext, times(1)).capture(messageCaptor.capture());
        var message = extractFirstMessageFromCaptor();

        assertThat(message.getFrom()).isEqualTo(juliet);
        assertThat(message.getTo()).isEqualTo(bob);
        assertThat(message.getLabel()).isEqualTo("GET /path");
        assertThat(message.getType()).isEqualTo(MessageType.SYNCHRONOUS);
        assertThat(message.getData().toString())
                .contains("Source-Name: juliet")
                .contains("Target-Name: bob");
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
        handler.handleResponse("200 OK", serviceNameHeaders, emptyMap(), "/path", "response body", Duration.ofMillis(3));

        verify(lsdContext, times(1)).capture(messageCaptor.capture());
        var message = extractFirstMessageFromCaptor();

        assertThat(message.getFrom()).isEqualTo(bob);
        assertThat(message.getTo()).isEqualTo(juliet);
        assertThat(message.getLabel()).isEqualTo("200 OK (3ms)");
        assertThat(message.getType()).isEqualTo(MessageType.SYNCHRONOUS_RESPONSE);
        assertThat(message.getDuration()).isEqualTo(Duration.ofMillis(3));
        assertThat(message.getData().toString())
                .contains("<h3>Request Headers</h3>")
                .contains("Target-Name: bob")
                .contains("Source-Name: juliet")
                .contains("<p>response body</p>");
    }
}