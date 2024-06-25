package io.lsdconsulting.interceptors.messaging.autoconfigure

import com.lsd.core.LsdContext
import io.lsdconsulting.interceptors.messaging.ErrorPublisherInterceptor
import io.lsdconsulting.interceptors.messaging.EventConsumerInterceptor
import io.lsdconsulting.interceptors.messaging.EventPublisherInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.integration.config.GlobalChannelInterceptor
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

    @Value("\${info.app.name}")
    private lateinit var appName: String
    private val lsdContext: LsdContext = LsdContext.instance
    @Bean
    @GlobalChannelInterceptor(patterns = ["*-in-*"], order = 100)
    open fun eventConsumerInterceptor(): EventConsumerInterceptor {
        return EventConsumerInterceptor(lsdContext, appName)
    }

    @Bean
    @GlobalChannelInterceptor(patterns = ["*-out-*"], order = 101)
    open fun eventPublisherInterceptor(): EventPublisherInterceptor {
        return EventPublisherInterceptor(lsdContext)
    }

    @Bean
    @GlobalChannelInterceptor(patterns = ["*errorChannel"], order = 101)
    open fun errorPublisherInterceptor(): ErrorPublisherInterceptor {
        return ErrorPublisherInterceptor(lsdContext, appName)
    }
}
