package io.lsdconsulting.interceptors.http

import io.lsdconsulting.interceptors.common.singleValueMap
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.time.Duration
import java.util.function.Consumer

const val RESPONSE_MAXY_BYTES = 10000

data class LsdOkHttpInterceptor(
    private var handlers: List<HttpInteractionHandler>
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestCopy = request.newBuilder().build()
        val path = request.url().encodedPath()
        val requestHeaders = singleValueMap(request.headers().toMultimap())
        handlers.forEach(Consumer { handler: HttpInteractionHandler ->
            handler.handleRequest(
                request.method(),
                requestHeaders,
                path,
                bodyToString(requestCopy)
            )
        })
        val start = System.currentTimeMillis()

        val response = chain.proceed(request)

        val duration = Duration.ofMillis(System.currentTimeMillis() - start)
        val responseHeaders = singleValueMap(response.headers().toMultimap())
        handlers.forEach(Consumer { handler: HttpInteractionHandler ->
            handler.handleResponse(
                response.code().toString() + " " + response.message(),
                requestHeaders,
                responseHeaders,
                path,
                copyBodyString(response),
                duration
            )
        })
        return response
    }

    /*
     * Prevent closing the response body stream by peeking. The max bytes is to prevent OOM for ridiculous size bodies
     */
    private fun copyBodyString(response: Response): String {
        return response.peekBody(RESPONSE_MAXY_BYTES.toLong()).string()
    }

    private fun bodyToString(copy: Request): String {
        val buffer = Buffer()
        copy.body()!!.writeTo(buffer)
        return buffer.readUtf8()
    }
}
