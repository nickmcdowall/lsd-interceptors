package io.lsdconsulting.interceptors.rabbitmq

import com.lsd.core.LsdContext
import com.lsd.core.domain.MessageType
import io.lsdconsulting.interceptors.common.HeaderKeys
import io.lsdconsulting.interceptors.common.log
import lsd.format.prettyPrint
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

/*
    This config adds the interception of messages to RabbitMq listeners
*/
@ConditionalOnBean(SimpleRabbitListenerContainerFactory::class)
@Configuration
open class RabbitListenerInterceptorConfig(
    private val simpleRabbitListenerContainerFactory: SimpleRabbitListenerContainerFactory,
    private val lsdContext: LsdContext
) {

    @PostConstruct
    fun postConstruct() {
        simpleRabbitListenerContainerFactory.setAfterReceivePostProcessors(MessagePostProcessor { message: Message ->
            postProcessMessage(
                message
            )
        })
    }

    private fun postProcessMessage(message: Message): Message {
        try {
            val exchangeName =
                deriveExchangeName(message.messageProperties, message.messageProperties.receivedExchange)
            val headers = retrieve(message)

            val payload = prettyPrint(message.body)
            val source = prettyPrint(headers[HeaderKeys.SOURCE_NAME.key()] ?: exchangeName)
            val target = prettyPrint(headers[HeaderKeys.TARGET_NAME.key()])

            lsdContext.capture(
                com.lsd.core.builders.MessageBuilder.messageBuilder()
                    .id(lsdContext.idGenerator.next())
                    .from(source)
                    .to(target)
                    .label("Consume event")
                    .data(renderHtmlFor(headers, payload))
                    .type(MessageType.ASYNCHRONOUS)
                    .build()
            )
        } catch (t: Throwable) {
            log().error(t.message, t)
        }
        return message
    }
}
