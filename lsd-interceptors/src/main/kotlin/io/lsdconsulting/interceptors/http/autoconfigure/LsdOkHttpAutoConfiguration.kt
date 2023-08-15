package io.lsdconsulting.interceptors.http.autoconfigure

import com.lsd.core.LsdContext
import io.lsdconsulting.interceptors.http.LsdOkHttpInterceptor
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler
import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import javax.annotation.PostConstruct

/**
 *
 *
 * If an [OkHttpClient.Builder] bean is available we can add an interceptor to the builder before it gets used to build
 * a `OkHttpClient`. We can't modify the client instance once it has been built without creating a separate instance so
 * we rely on the builder been being available.
 *
 * <br></br>
 *
 *
 * It is assumed that the [okhttp3.OkHttpClient] will be used to invoke downstream endpoints from within the app.
 * Therefore the *source* name will default to *'App'* and the *destination* name will be derived via
 * a [io.lsdconsulting.interceptors.http.naming.RegexResolvingNameMapper] by default.
 *
 * <br></br>
 *
 *
 * Users can override either or both of the default name mappings by supplying their own [io.lsdconsulting.interceptors.http.naming.SourceNameMappings] or
 * [io.lsdconsulting.interceptors.http.naming.DestinationNameMappings] beans and naming them *'defaultSourceNameMapping`* and *'defaultDestinationNameMapping`*.
 *
 */
@Configuration
@ConditionalOnClass(LsdContext::class)
@ConditionalOnBean(OkHttpClient.Builder::class)
@ConditionalOnProperty(
    value = ["lsd.interceptors.autoconfig.okhttp.enabled"],
    havingValue = "true"
) //TODO create new property
@Import(NamingConfig::class, HttpHandlerConfig::class)
open class LsdOkHttpAutoConfiguration(
    private val httpInteractionHandlers: List<HttpInteractionHandler>,
    private val okHttpClientBuilder: OkHttpClient.Builder,
){

    @PostConstruct
    fun configureInterceptor() {
        okHttpClientBuilder.addInterceptor(LsdOkHttpInterceptor(httpInteractionHandlers))
    }
}
