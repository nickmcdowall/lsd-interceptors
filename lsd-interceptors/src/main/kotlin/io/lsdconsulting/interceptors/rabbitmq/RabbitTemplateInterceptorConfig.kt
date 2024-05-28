package io.lsdconsulting.interceptors.rabbitmq

import com.lsd.core.LsdContext
import com.lsd.core.domain.MessageType
import io.lsdconsulting.interceptors.common.HeaderKeys.SOURCE_NAME
import io.lsdconsulting.interceptors.common.HeaderKeys.TARGET_NAME
import io.lsdconsulting.interceptors.common.log
import jakarta.annotation.PostConstruct
import lsd.format.prettyPrint
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
@ConditionalOnClass(RabbitTemplate::class, LsdContext::class)
open class RabbitTemplateInterceptorConfig(
    private val rabbitTemplates: List<RabbitTemplate>,
) {

    @Value("\${info.app.name}")
    private lateinit var appName: String

    private val lsdContext = LsdContext.instance

    @PostConstruct
    fun configureRabbitTemplatePublishInterceptor() {
        rabbitTemplates.forEach(Consumer { rabbitTemplate: RabbitTemplate ->
            rabbitTemplate.addBeforePublishPostProcessors(MessagePostProcessor { message: Message ->
                log().debug("Rabbit message properties before publishing:{}", message.messageProperties)
                val headers = retrieve(message)
                try {
                    val eventName = deriveEventName(message.messageProperties, rabbitTemplate.exchange)
                    val payload = prettyPrint(message.body)
                    val source = prettyPrint(message.messageProperties.headers[SOURCE_NAME.key()] ?: appName)
                    val target = prettyPrint(message.messageProperties.headers[TARGET_NAME.key()] ?: eventName)
                    lsdContext.capture(
                        com.lsd.core.builders.MessageBuilder.messageBuilder()
                            .id(lsdContext.idGenerator.next())
                            .from(source)
                            .to(target)
                            .label("Publish event")
                            .data(renderHtmlFor(headers, payload))
                            .type(MessageType.ASYNCHRONOUS)
                            .build()
                    )
                } catch (t: Throwable) {
                    log().error(t.message, t)
                }
                message
            })
            rabbitTemplate.addAfterReceivePostProcessors(MessagePostProcessor { message: Message ->
                log().debug("Rabbit message properties after receiving:{}", message.messageProperties)
                val headers = retrieve(message)
                try {
                    val eventName = deriveEventName(message.messageProperties, message.messageProperties.consumerQueue)
                    val payload = prettyPrint(message.body)
                    val source = prettyPrint(message.messageProperties.headers[TARGET_NAME.key()] ?: eventName)
                    val target = appName
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
                message
            })
        })
    }
}
