package io.lsdconsulting.interceptors.messaging

import com.lsd.core.LsdContext
import com.lsd.core.builders.MessageBuilder.Companion.messageBuilder
import com.lsd.core.domain.MessageType
import io.lsdconsulting.interceptors.common.HeaderKeys.TARGET_NAME
import lsd.format.prettyPrint
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.ChannelInterceptor

class EventConsumerInterceptor(
    private val lsdContext: LsdContext,
    @Value("\${info.app.name}") private var appName: String
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val payload = prettyPrint(message.payload)
        val source =prettyPrint(message.headers[TARGET_NAME.key()])
        val target = appName
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
