package io.lsdconsulting.interceptors.rabbitmq

import com.lsd.core.LsdContext
import com.lsd.core.builders.MessageBuilder.Companion.messageBuilder
import com.lsd.core.domain.MessageType
import io.lsdconsulting.interceptors.common.HeaderKeys.TARGET_NAME
import io.lsdconsulting.interceptors.common.log
import lsd.format.prettyPrint
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

/*
    This config adds the interception of messages to RabbitMq listeners
*/
@ConditionalOnClass(value = [LsdContext::class, SimpleRabbitListenerContainerFactory::class])
@ConditionalOnBean(SimpleRabbitListenerContainerFactory::class)
@Configuration
open class RabbitListenerInterceptorConfig(
    private val simpleRabbitListenerContainerFactory: SimpleRabbitListenerContainerFactory,
) {

    @Value("\${info.app.name}")
    private lateinit var appName: String

    private val lsdContext = LsdContext.instance

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
            val eventName = deriveEventName(message.messageProperties, message.messageProperties.consumerQueue)
            val headers = retrieve(message)

            val payload = prettyPrint(message.body)
            val source = prettyPrint(headers[TARGET_NAME.key()] ?: eventName)
            val target = appName

            lsdContext.capture(
                messageBuilder()
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
