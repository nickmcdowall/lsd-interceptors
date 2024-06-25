package io.lsdconsulting.interceptors.http

import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class LsdOkHttpInterceptorTest {
    private val requestBodyString = "{\"name\":\"nick\"}"
    private val responseBodyString = "{}"
    private val okResponse = anOkResponse()

    private val chain = mockk<Interceptor.Chain>()
    private val handler = mockk<HttpInteractionHandler>(relaxed = true)

    private lateinit var okHttpInterceptor: Interceptor

    @BeforeEach
    fun setUp() {
        okHttpInterceptor = LsdOkHttpInterceptor(listOf(handler))
        every { chain.request() } returns requestFor("PUT", "/user")
        every { chain.proceed(any()) } returns okResponse
    }

    @Test
    fun delegatesMessageHandling() {
        okHttpInterceptor.intercept(chain)
        verify { handler.handleRequest("PUT", emptyMap(), "/user", requestBodyString) }
        verify {
            handler.handleResponse(
                eq("200 OK"),
                eq(emptyMap()),
                eq(emptyMap()),
                eq("/user"),
                eq(responseBodyString),
                any<Duration>()
            )
        }
    }

    @Test
    fun requestIsStillIntactAfterIntercept() {
        okHttpInterceptor.intercept(chain)
        val buffer = Buffer()

        chain.request().body!!.writeTo(buffer)

        assertThat(buffer.readUtf8()).isEqualTo(requestBodyString)
    }

    @Test
    fun returnsExpectedResponse() {
        val response = okHttpInterceptor.intercept(chain)
        assertThat(response).isEqualTo(okResponse)
    }

    @Test
    fun doesNotCloseResponseBody() {
        every { chain.proceed(any()) } returns anOkResponse()

        val response = okHttpInterceptor.intercept(chain)

        assertThat(response.body!!.string()).isEqualTo(responseBodyString)
    }

    private fun requestFor(method: String, path: String): Request {
        return Request.Builder()
            .url("https://localhost:8080$path")
            .method(method, requestBodyString.toRequestBody(MEDIA_TYPE))
            .build()
    }

    private fun anOkResponse(): Response {
        return Response.Builder()
            .protocol(Protocol.HTTP_2)
            .code(200)
            .message("OK")
            .request(requestFor("PUT", "/user"))
            .body(responseBodyString.toByteArray().toResponseBody(MEDIA_TYPE))
            .build()
    }

    companion object {
        val MEDIA_TYPE = "application/json".toMediaTypeOrNull()
    }
}
