package io.lsdconsulting.lsd.distributed.interceptor.config

import com.lsd.core.LsdContext
import com.lsd.core.domain.MessageType
import io.lsdconsulting.interceptors.common.HeaderKeys
import io.lsdconsulting.interceptors.common.log
import io.lsdconsulting.interceptors.messaging.convertToString
import io.lsdconsulting.interceptors.rabbitmq.deriveExchangeName
import io.lsdconsulting.interceptors.rabbitmq.renderHtmlFor
import io.lsdconsulting.interceptors.rabbitmq.retrieve
import lsd.format.prettyPrint
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer
import javax.annotation.PostConstruct

@Configuration
@ConditionalOnClass(RabbitTemplate::class)
open class RabbitTemplateInterceptorConfig(
    private val rabbitTemplates: List<RabbitTemplate>,
    private val appName: String,
    private val lsdContext: LsdContext,
) {

    @PostConstruct
    fun configureRabbitTemplatePublishInterceptor() {
        rabbitTemplates.forEach(Consumer { rabbitTemplate: RabbitTemplate ->
            rabbitTemplate.addBeforePublishPostProcessors(MessagePostProcessor { message: Message ->
                log().info(
                    "Rabbit message properties before publishing:{}",
                    message.messageProperties
                )
                val headers = retrieve(message)
                try {
                    val exchangeName = deriveExchangeName(message.messageProperties, rabbitTemplate.exchange)
                    val payload = prettyPrint(message.body)
                    val source = convertToString(message.messageProperties.headers[HeaderKeys.SOURCE_NAME.key()] ?: appName)
                    val target = convertToString(message.messageProperties.headers[HeaderKeys.TARGET_NAME.key()] ?: exchangeName)
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
                log().info(
                    "Rabbit message properties after receiving:{}",
                    message.messageProperties
                )
                val headers = retrieve(message)
                try {
                    val exchangeName = deriveExchangeName(
                        message.messageProperties,
                        message.messageProperties.receivedExchange
                    )
                    val payload = prettyPrint(message.body)
                    val source = convertToString(message.messageProperties.headers[HeaderKeys.SOURCE_NAME.key()] ?: exchangeName)
                    val target = convertToString(message.messageProperties.headers[HeaderKeys.TARGET_NAME.key()] ?: appName)
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
        })
    }
}
