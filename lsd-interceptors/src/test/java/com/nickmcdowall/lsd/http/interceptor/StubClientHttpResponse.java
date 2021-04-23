package com.nickmcdowall.lsd.http.interceptor;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.InputStream;

@Builder
@RequiredArgsConstructor
public class StubClientHttpResponse implements ClientHttpResponse {

    private final InputStream body;
    private final HttpHeaders headers;
    private final HttpStatus statusCode;

    @Override
    public HttpStatus getStatusCode() {
        return statusCode;
    }

    @Override
    public int getRawStatusCode() {
        return statusCode.value();
    }

    @Override
    public String getStatusText() {
        return statusCode.value() + statusCode.getReasonPhrase();
    }

    @SneakyThrows
    @Override
    public void close() {
        body.close();
    }

    @Override
    public InputStream getBody() {
        return body;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }
}