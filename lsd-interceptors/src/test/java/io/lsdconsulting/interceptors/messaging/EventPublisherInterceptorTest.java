package io.lsdconsulting.interceptors.messaging;

import com.lsd.LsdContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;

import java.util.Map;

import static io.lsdconsulting.interceptors.common.Headers.HeaderKeys.SOURCE_NAME;
import static io.lsdconsulting.interceptors.common.Headers.HeaderKeys.TARGET_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EventPublisherInterceptorTest {
    private final ArgumentCaptor<String> patternCaptor = ArgumentCaptor.forClass(String.class);
    private final ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

    private final LsdContext lsdContext = mock(LsdContext.class);
    private final Message<byte[]> message = mock(Message.class);

    private final EventPublisherInterceptor underTest = new EventPublisherInterceptor(lsdContext);

    @Test
    void logInteraction() {
        given(message.getPayload()).willReturn("{\"key\":\"value\"}".getBytes(UTF_8));
        given(message.getHeaders()).willReturn(new MessageHeaders(Map.of(SOURCE_NAME.key(), "Source", TARGET_NAME.key(), "Target")));

        underTest.preSend(message, mock(MessageChannel.class));

        verify(lsdContext).capture(patternCaptor.capture(), payloadCaptor.capture());
        assertThat(patternCaptor.getValue()).isEqualTo("Publish event from Source to Target");
        assertThat(payloadCaptor.getValue()).isEqualTo(
                "<p><p><h4>Message Headers</h4><code>Target-Name: Target\n" +
                        "Source-Name: Source\n" +
                        "id: " + message.getHeaders().get("id") + "\n" +
                        "timestamp: " + message.getHeaders().get("timestamp") + "</code></p><p><h4>Body</h4><code>{\n" +
                        "  &quot;key&quot;: &quot;value&quot;\n" +
                        "}</code></p></p>"
        );
    }
}
