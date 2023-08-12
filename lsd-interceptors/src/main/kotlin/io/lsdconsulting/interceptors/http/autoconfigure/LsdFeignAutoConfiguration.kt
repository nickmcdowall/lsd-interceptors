package io.lsdconsulting.interceptors.http.autoconfigure

import com.lsd.core.LsdContext
import feign.Logger.Level
import io.lsdconsulting.interceptors.http.LsdFeignLoggerInterceptor
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.openfeign.FeignClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource

/**
 *
 *
 * If a [LsdContext] class is available it will automatically autoconfig a [LsdFeignLoggerInterceptor]
 *
 * <br></br>
 *
 *
 * By default *source* name defaults to *'App'* and the *destination* name will be derived using a
 * [io.lsdconsulting.interceptors.http.naming.RegexResolvingNameMapper].
 *
 * <br></br>
 *
 *
 * Users can override either or both of the default name mappings by supplying their own [io.lsdconsulting.interceptors.http.naming.SourceNameMappings] or
 * [io.lsdconsulting.interceptors.http.naming.DestinationNameMappings] beans and naming them *'defaultSourceNameMapping`* and *'defaultDestinationNameMapping`*.
 *
 */
@Configuration
@PropertySource(ignoreResourceNotFound = true, value = ["classpath:lsd.properties"])
@ConditionalOnProperty(name = ["lsd.interceptors.autoconfig.enabled"], havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(value = [LsdContext::class, FeignClientBuilder::class, Level::class])
@Import(NamingConfig::class, HttpHandlerConfig::class)
open class LsdFeignAutoConfiguration(
    private val httpInteractionHandlers: List<HttpInteractionHandler>
) {

    @Bean
    open fun lsdFeignLoggerInterceptor() = LsdFeignLoggerInterceptor(httpInteractionHandlers)

    @Bean
    @ConditionalOnMissingBean
    open fun feignLoggerLevel(): Level = Level.BASIC
}
