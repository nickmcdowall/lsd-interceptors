package io.lsdconsulting.interceptors.http

import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.*
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.Duration

class LsdOkHttpInterceptorTest {
    private val requestBodyString = "{\"name\":\"nick\"}"
    private val responseBodyString = "{}"
    private val okResponse = anOkResponse()

    private val chain = mockk<Interceptor.Chain>()
    private val handler = mockk<HttpInteractionHandler>(relaxed = true)

    private lateinit var okHttpInterceptor: Interceptor

    @BeforeEach
    @Throws(IOException::class)
    fun setUp() {
        okHttpInterceptor = LsdOkHttpInterceptor(listOf(handler))
        every { chain.request() } returns requestFor("PUT", "/user")
        every { chain.proceed(any()) } returns okResponse
    }

    @Test
    @Throws(IOException::class)
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
    @Throws(IOException::class)
    fun requestIsStillIntactAfterIntercept() {
        okHttpInterceptor.intercept(chain)
        val buffer = Buffer()
        chain.request().body()!!.writeTo(buffer)
        assertThat(buffer.readUtf8()).isEqualTo(requestBodyString)
    }

    @Test
    @Throws(IOException::class)
    fun returnsExpectedResponse() {
        val response = okHttpInterceptor.intercept(chain)
        assertThat(response).isEqualTo(okResponse)
    }

    @Test
    @Throws(IOException::class)
    fun doesNotCloseResponseBody() {
        every { chain.proceed(any()) } returns anOkResponse()
        val response = okHttpInterceptor.intercept(chain)
        assertThat(response.body()!!.string()).isEqualTo(responseBodyString)
    }

    private fun requestFor(method: String, path: String): Request {
        return Request.Builder()
            .url("https://localhost:8080$path")
            .method(method, RequestBody.create(MEDIA_TYPE, requestBodyString))
            .build()
    }

    private fun anOkResponse(): Response {
        return Response.Builder()
            .protocol(Protocol.HTTP_2)
            .code(200)
            .message("OK")
            .request(requestFor("PUT", "/user"))
            .body(ResponseBody.create(MEDIA_TYPE, responseBodyString.toByteArray()))
            .build()
    }

    companion object {
        val MEDIA_TYPE = MediaType.parse("application/json")
    }
}
