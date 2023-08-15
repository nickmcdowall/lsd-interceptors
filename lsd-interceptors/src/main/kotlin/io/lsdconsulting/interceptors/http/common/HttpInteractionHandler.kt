package io.lsdconsulting.interceptors.http.common

import java.time.Duration

interface HttpInteractionHandler {
    fun handleRequest(method: String, requestHeaders: Map<String, String>, path: String, body: String)
    fun handleResponse(
        statusMessage: String,
        requestHeaders: Map<String, String>,
        responseHeaders: Map<String, String>,
        path: String,
        body: String,
        duration: Duration
    )
}
