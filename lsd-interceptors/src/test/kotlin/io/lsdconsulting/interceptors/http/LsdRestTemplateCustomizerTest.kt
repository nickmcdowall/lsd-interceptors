package io.lsdconsulting.interceptors.http

import com.lsd.core.LsdContext
import io.lsdconsulting.interceptors.http.common.DefaultHttpInteractionHandler
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

internal class LsdRestTemplateCustomizerTest {
    private val restTemplate = RestTemplate()
    private val lsdContext: LsdContext = LsdContext.instance
    private val httpInteractionHandlers = listOf<HttpInteractionHandler>(
        DefaultHttpInteractionHandler(lsdContext, { "Source" }, { "Destination" })
    )
    private val lsdRestTemplateInterceptor = LsdRestTemplateInterceptor(httpInteractionHandlers)
    private val lsdRestTemplateCustomizer = LsdRestTemplateCustomizer(lsdRestTemplateInterceptor)

    @Test
    fun addsLsdInterceptor() {
        lsdRestTemplateCustomizer.customize(restTemplate)
        Assertions.assertThat(restTemplate.interceptors).containsExactly(lsdRestTemplateInterceptor)
    }

    @Test
    fun preservesExistingCustomizers() {
        restTemplate.interceptors = listOf(
            mockk<ClientHttpRequestInterceptor>()
        )
        lsdRestTemplateCustomizer.customize(restTemplate)
        Assertions.assertThat(restTemplate.interceptors).hasSize(2)
    }

    @Test
    fun doesntAddDuplicateInterceptor() {
        restTemplate.interceptors = listOf<ClientHttpRequestInterceptor>(lsdRestTemplateInterceptor)
        lsdRestTemplateCustomizer.customize(restTemplate)
        Assertions.assertThat(restTemplate.interceptors).hasSize(1)
    }

    @Test
    fun responseIsNotEmptyAfterInterception() {
        lsdRestTemplateCustomizer.customize(restTemplate)
        val forObject = restTemplate.getForObject("https://httpbin.org/get", String::class.java)
        Assertions.assertThat(forObject).isNotEmpty()
    }
}
