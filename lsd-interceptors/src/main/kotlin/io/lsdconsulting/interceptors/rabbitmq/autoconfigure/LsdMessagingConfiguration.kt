package io.lsdconsulting.interceptors.rabbitmq.autoconfigure

import com.lsd.core.LsdContext
import io.lsdconsulting.interceptors.rabbitmq.RabbitListenerInterceptorConfig
import io.lsdconsulting.lsd.distributed.interceptor.config.RabbitTemplateInterceptorConfig
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.messaging.support.ChannelInterceptor

/**
 *
 * If a [LsdContext] and ChannelInterceptor classes is available it will automatically autoconfig a [LsdMessagingConfiguration]
 *
 */
@Configuration
@PropertySource(ignoreResourceNotFound = true, value = ["classpath:lsd.properties"])
@ConditionalOnClass(value = [LsdContext::class, ChannelInterceptor::class])
@ConditionalOnProperty(name = ["lsd.interceptors.autoconfig.enabled"], havingValue = "true", matchIfMissing = true)
open class LsdMessagingConfiguration {

    @Autowired
    private lateinit var simpleRabbitListenerContainerFactory: SimpleRabbitListenerContainerFactory

    @Autowired
    private lateinit var rabbitTemplates: List<RabbitTemplate>

    @Value("\${info.app.name}")
    private lateinit var appName: String

    private val lsdContext: LsdContext = LsdContext.instance

    @Bean
    open fun rabbitListenerInterceptorConfig() = RabbitListenerInterceptorConfig(simpleRabbitListenerContainerFactory, lsdContext)

    @Bean
    open fun rabbitTemplateInterceptorConfig() = RabbitTemplateInterceptorConfig(rabbitTemplates, appName, lsdContext)
}
