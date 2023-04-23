package io.lsdconsulting.interceptors.messaging;

import com.lsd.core.LsdContext;
import com.lsd.core.domain.MessageType;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;

import java.util.Map;

import static com.lsd.core.domain.ParticipantType.PARTICIPANT;
import static io.lsdconsulting.interceptors.common.Headers.HeaderKeys.SOURCE_NAME;
import static io.lsdconsulting.interceptors.common.Headers.HeaderKeys.TARGET_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class EventConsumerInterceptorTest {
    private final ArgumentCaptor<com.lsd.core.domain.Message> messageCaptor = ArgumentCaptor.forClass(com.lsd.core.domain.Message.class);

    private final LsdContext lsdContext = spy(LsdContext.class);
    private final Message<byte[]> message = spy(Message.class);

    private final EventConsumerInterceptor underTest = new EventConsumerInterceptor(lsdContext);

    @Test
    void logInteraction() {
        given(message.getPayload()).willReturn("{\"key\":\"value\"}".getBytes(UTF_8));
        given(message.getHeaders()).willReturn(new MessageHeaders(Map.of(SOURCE_NAME.key(), "Source", TARGET_NAME.key(), "Target")));

        underTest.preSend(message, mock(MessageChannel.class));

        verify(lsdContext).capture(messageCaptor.capture());
        var capturedMessage = messageCaptor.getValue();

        AssertionsForClassTypes.assertThat(capturedMessage.getFrom()).isEqualTo(PARTICIPANT.called("Source"));
        AssertionsForClassTypes.assertThat(capturedMessage.getTo()).isEqualTo(PARTICIPANT.called("Target"));
        AssertionsForClassTypes.assertThat(capturedMessage.getLabel()).isEqualTo("Consume event");
        AssertionsForInterfaceTypes.assertThat(capturedMessage.getType()).isEqualTo(MessageType.ASYNCHRONOUS);
        AssertionsForClassTypes.assertThat(capturedMessage.getData().toString())
                .contains("timestamp: " + message.getHeaders().get("timestamp"))
                .contains("Source-Name: Source")
                .contains("Target-Name: Target")
                .contains("id: " + message.getHeaders().get("id"));
    }
}