package io.lsdconsulting.interceptors.messaging

import com.lsd.core.LsdContext
import com.lsd.core.builders.MessageBuilder.Companion.messageBuilder
import com.lsd.core.domain.MessageType
import io.lsdconsulting.interceptors.common.HeaderKeys
import lsd.format.prettyPrint
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.ChannelInterceptor

class EventConsumerInterceptor(
    private val lsdContext: LsdContext
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val payload = prettyPrint(message.payload)
        val source = convertToString(message.headers[HeaderKeys.SOURCE_NAME.key()])
        val target = convertToString(message.headers[HeaderKeys.TARGET_NAME.key()])
        lsdContext.capture(
            messageBuilder()
                .id(lsdContext.idGenerator.next())
                .from(source)
                .to(target)
                .label("Consume event")
                .data(renderHtmlFor(message.headers, payload))
                .type(MessageType.ASYNCHRONOUS)
                .build()
        )
        return message
    }
}
