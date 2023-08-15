package io.lsdconsulting.interceptors.http

import feign.Request
import feign.RequestTemplate
import feign.Response
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.IOException
import java.nio.charset.Charset
import java.time.Duration
import java.util.function.Consumer

/**
 * protected methods so extending to gain access
 */
class LsdFeignLoggerInterceptorTest : LsdFeignLoggerInterceptor(
    listOf(mockk<HttpInteractionHandler>(relaxed = true))
) {
    private val headers = emptyMap<String, Collection<String>>()
    private val level = Level.BASIC

    @Test
    fun logsRequest() {
        logRequest("configKey", level, requestWithBody("body"))
        handlers.forEach(Consumer { handler: HttpInteractionHandler ->
            verify { handler.handleRequest("GET", emptyMap(), "/app-endpoint/something", "body") }
        })
    }

    @Test
    fun handlesEmptyBody() {
        logRequest("configKey", level, requestWithBody(null))
        handlers.forEach(Consumer { handler: HttpInteractionHandler ->
            verify { handler.handleRequest("GET", emptyMap(), "/app-endpoint/something", "") }
        })
    }

    @Test
    fun handlesPathParameters() {
        logRequest("configKey", level, requestWithParameter("/app-endpoint/something?someParam=hi&secondParam=yo"))
        handlers.forEach(Consumer { handler: HttpInteractionHandler ->
            verify { handler.handleRequest("GET", emptyMap(), "/app-endpoint/something?someParam=hi&secondParam=yo", "") }
        })
    }

    @Test
    @Throws(IOException::class)
    fun capturesResponseInteraction() {
        logAndRebufferResponse(
            "configKey", level, Response.builder()
                .request(requestWithBody("body"))
                .body("response body".toByteArray())
                .status(200)
                .build(), 1
        )
        handlers.forEach(Consumer { handler: HttpInteractionHandler ->
            verify {
                handler.handleResponse(
                    match { it.startsWith("200 OK") },
                    eq(emptyMap()),
                    eq(emptyMap()),
                    eq("/app-endpoint/something"),
                    eq("response body"),
                    any<Duration>()
                )
            }
        })
    }

    @Test
    @Throws(IOException::class)
    fun handleHttpRequest() {
        logAndRebufferResponse(
            "configKey", level, Response.builder()
                .request(requestWithBody("body", "http"))
                .body("response body".toByteArray())
                .status(200)
                .build(), 1
        )
        handlers.forEach(Consumer { handler: HttpInteractionHandler ->
            verify {
                handler.handleResponse(
                    match { it.startsWith("200 OK") },
                    eq(emptyMap()),
                    eq(emptyMap()),
                    eq("/app-endpoint/something"),
                    eq("response body"),
                    any<Duration>()
                )
            }
        })
    }

    @Test
    @Throws(IOException::class)
    fun handlesUnresolvedStatusCode() {
        logAndRebufferResponse(
            "configKey", level, Response.builder()
                .request(requestWithBody("body"))
                .body("response body".toByteArray())
                .status(111)
                .build(), 1
        )
        handlers.forEach(Consumer { handler: HttpInteractionHandler ->
            verify {
                handler.handleResponse(
                    match { it.startsWith("<unresolved status:111>") },
                    eq(emptyMap()),
                    eq(emptyMap()),
                    eq("/app-endpoint/something"),
                    eq("response body"),
                    any<Duration>()
                )
            }
        })
    }

    @Test
    @Throws(IOException::class)
    fun preservesResponseStream() {
        val response = logAndRebufferResponse(
            "configKey", level, Response.builder()
                .request(requestWithBody("request body"))
                .body("response body".toByteArray())
                .status(200)
                .build(), 1
        )
        assertThat(response.body().asInputStream()).hasContent("response body")
    }

    private fun requestWithBody(body: String?): Request {
        return Request.create(
            Request.HttpMethod.GET,
            "https://localhost:8080/app-endpoint/something",
            headers, body?.toByteArray(),
            Charset.defaultCharset(),
            RequestTemplate()
        )
    }

    private fun requestWithBody(body: String?, protocol: String): Request {
        return Request.create(
            Request.HttpMethod.GET,
            "$protocol://localhost:8080/app-endpoint/something",
            headers, body?.toByteArray(),
            Charset.defaultCharset(),
            RequestTemplate()
        )
    }

    private fun requestWithParameter(pathWithParam: String): Request {
        return Request.create(
            Request.HttpMethod.GET,
            "https://localhost:8080$pathWithParam",
            headers, null,
            Charset.defaultCharset(),
            RequestTemplate()
        )
    }
}
