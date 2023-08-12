package io.lsdconsulting.interceptors.http

import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.Duration
import java.util.function.Consumer

/**
 * Created to intercept rest template calls for lsd interactions.
 * Attempts to reset the input stream so that no data is lost on reading the reponse body
 */
data class LsdRestTemplateInterceptor(
    private var handlers: List<HttpInteractionHandler>
) : ClientHttpRequestInterceptor {

    @Throws(IOException::class)
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {

        val path = request.uri.toString()
        val requestHeaders = request.headers.toSingleValueMap()
        handlers.forEach(Consumer { handler: HttpInteractionHandler ->
            handler.handleRequest(
                request.methodValue,
                requestHeaders,
                path,
                String(body)
            )
        })
        val start = System.currentTimeMillis()

        val response = execution.execute(request, body)

        val duration = Duration.ofMillis(System.currentTimeMillis() - start)
        val responseHeaders = response.headers.toSingleValueMap()
        handlers.forEach(Consumer { handler: HttpInteractionHandler ->
            handler.handleResponse(
                deriveResponseStatus(
                    response
                ), requestHeaders, responseHeaders, path, copyBodyToString(response), duration
            )
        })
        return response
    }

    private fun deriveResponseStatus(response: ClientHttpResponse): String {
        return response.statusCode.toString()
    }

    private fun copyBodyToString(response: ClientHttpResponse): String {
        if (response.headers.contentLength == 0L) return ""
        val outputStream = ByteArrayOutputStream()
        val inputStream = response.body
        inputStream.transferTo(outputStream)
        return outputStream.toString()
    }
}
