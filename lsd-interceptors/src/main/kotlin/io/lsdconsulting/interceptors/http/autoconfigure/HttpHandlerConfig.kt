package io.lsdconsulting.interceptors.http.autoconfigure

import com.lsd.core.LsdContext
import io.lsdconsulting.interceptors.http.common.DefaultHttpInteractionHandler
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

internal class HttpHandlerConfig(
    private val defaultSourceNameMapping: SourceNameMappings,
    private val defaultDestinationNameMapping: DestinationNameMappings,
) {
    private val lsdContext: LsdContext = LsdContext.instance

    @Bean
    @ConditionalOnMissingBean(name = ["httpInteractionHandlers"])
    fun httpInteractionHandlers() = listOf<HttpInteractionHandler>(
        DefaultHttpInteractionHandler(
            lsdContext,
            defaultSourceNameMapping,
            defaultDestinationNameMapping
        )
    )
}
