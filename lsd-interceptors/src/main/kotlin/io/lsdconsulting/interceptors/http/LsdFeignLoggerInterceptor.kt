package io.lsdconsulting.interceptors.http

import feign.Logger.JavaLogger
import feign.Request
import feign.Response
import feign.Util
import io.lsdconsulting.interceptors.common.singleValueMap
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler
import org.springframework.http.HttpStatus
import java.io.IOException
import java.time.Duration
import java.util.*
import java.util.function.Consumer

const val EXTRACT_PATH = "https?://.*?(/.*)"

/**
 * Intercepts Feign [Request] and [Response] messages to add them to the [com.lsd.core.LsdContext] class.
 *
 *
 * (This allows lsd-core to display them on the sequence diagrams).
 */
open class LsdFeignLoggerInterceptor(val handlers: List<HttpInteractionHandler>) : JavaLogger(
    LsdFeignLoggerInterceptor::class.java
) {
    override fun logRequest(configKey: String, level: Level, request: Request) {
        super.logRequest(configKey, level, request)
        captureRequestInteraction(request)
    }

    @Throws(IOException::class)
    override fun logAndRebufferResponse(
        configKey: String,
        logLevel: Level,
        response: Response,
        elapsedTime: Long
    ): Response {
        super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime)
        val body = extractResponseBodyToString(response)
        captureResponseInteraction(response, body, Duration.ofMillis(elapsedTime))
        return resetBodyData(response, body.toByteArray())
    }

    private fun captureRequestInteraction(request: Request) {
        val bodyData = Optional.ofNullable(request.body())
        val body = bodyData.map { bytes: ByteArray? ->
            String(
                bytes!!
            )
        }.orElse("")
        val path = derivePath(request.url())
        val headers = singleValueMap(request.headers())
        handlers.forEach(Consumer { handler: HttpInteractionHandler ->
            handler.handleRequest(
                request.httpMethod().name,
                headers,
                path,
                body
            )
        })
    }

    private fun captureResponseInteraction(response: Response, body: String, duration: Duration) {
        val path = derivePath(response.request().url())
        val requestHeaders = singleValueMap(response.request().headers())
        val responseHeaders = singleValueMap(response.headers())
        handlers.forEach(Consumer { handler: HttpInteractionHandler ->
            handler.handleResponse(
                deriveStatus(response.status()),
                requestHeaders,
                responseHeaders,
                path,
                body,
                duration
            )
        })
    }

    private fun deriveStatus(code: Int): String =
        HttpStatus.resolve(code)?.let { obj: HttpStatus -> obj.toString() }
            ?: String.format("<unresolved status:%s>", code)

    private fun derivePath(url: String): String = url.replace(EXTRACT_PATH.toRegex(), "$1")

    @Throws(IOException::class)
    private fun extractResponseBodyToString(response: Response): String {
        return response.body()?.let { String(Util.toByteArray(it.asInputStream())) } ?: ""
    }

    private fun resetBodyData(response: Response, bodyData: ByteArray): Response {
        return response.toBuilder().body(bodyData).build()
    }
}
