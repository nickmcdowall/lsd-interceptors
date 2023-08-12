package io.lsdconsulting.interceptors.http;

import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.InputStream;

public class StubClientHttpResponse implements ClientHttpResponse {

    private final InputStream body;
    private final HttpHeaders headers;
    private final HttpStatus statusCode;

    public StubClientHttpResponse(InputStream body, HttpHeaders headers, HttpStatus statusCode) {
        this.body = body;
        this.headers = headers;
        this.statusCode = statusCode;
    }

    public static StubClientHttpResponseBuilder builder() {
        return new StubClientHttpResponseBuilder();
    }

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

    public static class StubClientHttpResponseBuilder {
        private InputStream body;
        private HttpHeaders headers;
        private HttpStatus statusCode;

        StubClientHttpResponseBuilder() {
        }

        public StubClientHttpResponseBuilder body(InputStream body) {
            this.body = body;
            return this;
        }

        public StubClientHttpResponseBuilder headers(HttpHeaders headers) {
            this.headers = headers;
            return this;
        }

        public StubClientHttpResponseBuilder statusCode(HttpStatus statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public StubClientHttpResponse build() {
            return new StubClientHttpResponse(this.body, this.headers, this.statusCode);
        }

        public String toString() {
            return "StubClientHttpResponse.StubClientHttpResponseBuilder(body=" + this.body + ", headers=" + this.headers + ", statusCode=" + this.statusCode + ")";
        }
    }
}