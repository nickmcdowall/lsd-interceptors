package io.lsdconsulting.interceptors.messaging;

import com.lsd.LsdContext;
import com.lsd.diagram.ValidComponentName;
import lombok.RequiredArgsConstructor;
import lsd.format.PrettyPrinter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

import static io.lsdconsulting.interceptors.http.common.Headers.HeaderKeys.SOURCE_NAME;
import static io.lsdconsulting.interceptors.http.common.Headers.HeaderKeys.TARGET_NAME;

@RequiredArgsConstructor
public class EventPublisherInterceptor implements ChannelInterceptor {

    private final LsdContext lsdContext;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        String payload = PrettyPrinter.prettyPrint(new String((byte[]) message.getPayload()));
        String source = (String) message.getHeaders().get(SOURCE_NAME.key());
        String target = (String) message.getHeaders().get(TARGET_NAME.key());

        lsdContext.capture("Publish event from " + ValidComponentName.of(source) + " to " + ValidComponentName.of(target), payload);

        return message;
    }
}
