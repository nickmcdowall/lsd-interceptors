package io.lsdconsulting.interceptors.messaging

import com.lsd.core.LsdContext
import com.lsd.core.builders.MessageBuilder.Companion.messageBuilder
import com.lsd.core.domain.MessageType
import com.lsd.core.sanitise
import lombok.RequiredArgsConstructor
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.ChannelInterceptor

@RequiredArgsConstructor
class ErrorPublisherInterceptor(
    private val lsdContext: LsdContext,
    private val appName: String,
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val payload = message.payload.toString()
        val target = (channel as PublishSubscribeChannel).fullChannelName.sanitise()
        lsdContext.capture(
            messageBuilder()
                .id(lsdContext.idGenerator.next())
                .from(appName)
                .to(target)
                .label("Publish error event")
                .data(renderHtmlFor(message.headers, payload))
                .type(MessageType.ASYNCHRONOUS)
                .build()
        )
        return message
    }
}
