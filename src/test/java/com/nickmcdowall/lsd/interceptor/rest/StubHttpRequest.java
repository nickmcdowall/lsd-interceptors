package com.nickmcdowall.lsd.interceptor.rest;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;

import java.net.URI;

@Builder
@RequiredArgsConstructor
public class StubHttpRequest implements HttpRequest {

    private final String methodValue;
    private final URI uri;
    private final HttpHeaders httpHeaders;

    @Override
    public HttpHeaders getHeaders() {
        return httpHeaders;
    }

    @Override
    public String getMethodValue() {
        return methodValue;
    }

    @Override
    public URI getURI() {
        return uri;
    }
}