package io.lsdconsulting.interceptors.http.autoconfigure

import io.lsdconsulting.interceptors.common.AppName.Factory.create
import io.lsdconsulting.interceptors.http.naming.AlwaysAppName
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings
import io.lsdconsulting.interceptors.http.naming.RegexResolvingNameMapper
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

internal class NamingConfig {
    @Value("\${info.app.name:App}")
    private lateinit var appName: String

    @Bean
    @ConditionalOnMissingBean(name = ["defaultSourceNameMapping"])
    fun defaultSourceNameMapping(): SourceNameMappings {
        return AlwaysAppName(create(appName))
    }

    @Bean
    @ConditionalOnMissingBean(name = ["defaultDestinationNameMapping"])
    fun defaultDestinationNameMapping(): DestinationNameMappings {
        return RegexResolvingNameMapper()
    }
}
