package io.lsdconsulting.interceptors.http

import io.lsdconsulting.interceptors.http.StubClientHttpResponse.StubClientHttpResponseBuilder
import io.lsdconsulting.interceptors.http.StubHttpRequest.StubHttpRequestBuilder
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpResponse
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.time.Duration

class LsdRestTemplateInterceptorTest {
    private val path = "/price/watch"
    private val uri = URI.create(path)
    private val requestBodyString = "a request body"
    private val requestBodyBytes = requestBodyString.toByteArray()
    private val stubHttpRequest = aGetRequest(uri).build()
    private val responseBodyString = "a response body"
    private val responseBodyStream: InputStream = ByteArrayInputStream(responseBodyString.toByteArray())
    private val httpResponse: ClientHttpResponse = aStubbedOkResponse().build()

    private val execution = mockk<ClientHttpRequestExecution>()
    private val handler = mockk<HttpInteractionHandler>(relaxed = true)

    private lateinit var interceptor: LsdRestTemplateInterceptor

    @BeforeEach
    @Throws(IOException::class)
    fun setUp() {
        interceptor = LsdRestTemplateInterceptor(listOf(handler))
        every { execution.execute(any(), any()) } returns httpResponse
    }

    @Test
    @Throws(IOException::class)
    fun passActualRequestToExecutor() {
        val request: HttpRequest = stubHttpRequest
        interceptor.intercept(request, requestBodyBytes, execution)
        verify { execution.execute(request, requestBodyBytes) }
    }

    @Test
    @Throws(IOException::class)
    fun returnsActualResponse() {
        val interceptedResponse = interceptor.intercept(stubHttpRequest, requestBodyBytes, execution)
        assertThat(interceptedResponse).isEqualTo(httpResponse)
    }

    @Test
    @Throws(IOException::class)
    fun logRequestInteraction() {
        interceptor.intercept(stubHttpRequest, requestBodyBytes, execution)
        verify { handler.handleRequest("GET", emptyMap(), path, requestBodyString) }
    }

    @Test
    @Throws(IOException::class)
    fun logResponseInteraction() {
        interceptor.intercept(stubHttpRequest, requestBodyBytes, execution)
        verify {
            handler.handleResponse(
                eq("200 OK"),
                eq(emptyMap()),
                eq(emptyMap()),
                eq(path),
                eq(responseBodyString),
                any<Duration>()
            )
        }
    }

    @Test
    @Throws(IOException::class)
    fun handleUnknownDestinationMappingByFallingBackToPathNameResolver() {
        val request: HttpRequest = aGetRequest(uri).uri(URI.create("/another/path")).build()
        interceptor.intercept(request, requestBodyBytes, execution)
        verify { handler.handleRequest("GET", emptyMap(), "/another/path", requestBodyString) }
        verify {
            handler.handleResponse(
                eq("200 OK"),
                eq(emptyMap()),
                eq(emptyMap()),
                eq("/another/path"),
                eq(responseBodyString),
                any<Duration>()
            )
        }
    }

    @Test
    @Throws(IOException::class)
    fun emptyStringOnZeroLengthResponse() {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentLength = 0
        every { execution.execute(any(), any()) } returns aStubbedOkResponse().headers(httpHeaders).build()
        interceptor.intercept(stubHttpRequest, requestBodyBytes, execution)
        verify {
            handler.handleResponse(
                eq("200 OK"),
                eq(emptyMap()),
                eq(mapOf("Content-Length" to "0")),
                eq("/price/watch"),
                eq(""),
                any<Duration>()
            )
        }
    }

    @Test
    @Throws(IOException::class)
    fun removesPathParametersFromUri() {
        interceptor.intercept(aGetRequest(URI.create("/cow?param=yes")).build(), requestBodyBytes, execution)
        verify { handler.handleRequest("GET", emptyMap(), "/cow?param=yes", requestBodyString) }
        verify {
            handler.handleResponse(
                eq("200 OK"),
                eq(emptyMap()),
                eq(emptyMap()),
                eq("/cow?param=yes"),
                eq(responseBodyString),
                any<Duration>()
            )
        }
    }

    private fun aStubbedOkResponse(): StubClientHttpResponseBuilder {
        return StubClientHttpResponse.builder()
            .body(responseBodyStream)
            .statusCode(HttpStatus.OK)
            .headers(HttpHeaders.EMPTY)
    }

    private fun aGetRequest(uri: URI): StubHttpRequestBuilder {
        return StubHttpRequest.builder()
            .uri(uri).methodValue("GET").httpHeaders(HttpHeaders.EMPTY)
    }
}