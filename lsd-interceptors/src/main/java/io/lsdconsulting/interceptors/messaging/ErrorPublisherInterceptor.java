package io.lsdconsulting.interceptors.messaging;

import com.lsd.core.LsdContext;
import lombok.RequiredArgsConstructor;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

import static com.lsd.core.builders.MessageBuilder.messageBuilder;
import static com.lsd.core.domain.MessageType.ASYNCHRONOUS;
import static io.lsdconsulting.interceptors.messaging.HtmlRenderer.renderHtmlFor;

@RequiredArgsConstructor
public class ErrorPublisherInterceptor implements ChannelInterceptor {

    private final LsdContext lsdContext;
    private final String appName;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        String payload = message.getPayload().toString();
        String target = ((PublishSubscribeChannel) channel).getFullChannelName().replace(".", "").replace("-", "");

        lsdContext.capture(messageBuilder()
                .id(lsdContext.getIdGenerator().next())
                .from(appName)
                .to(target)
                .label("Publish error event")
                .data(renderHtmlFor(message.getHeaders(), payload))
                .type(ASYNCHRONOUS)
                .build());

        return message;
    }
}
