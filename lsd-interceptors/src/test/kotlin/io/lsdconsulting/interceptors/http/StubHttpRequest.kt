package io.lsdconsulting.interceptors.http

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import java.net.URI

data class StubHttpRequest(
    private val methodValue: String,
    private val uri: URI,
    private val httpHeaders: HttpHeaders
) :
    HttpRequest {

    override fun getHeaders(): HttpHeaders {
        return httpHeaders
    }

    override fun getMethodValue(): String {
        return methodValue
    }

    override fun getURI(): URI {
        return uri
    }
}
