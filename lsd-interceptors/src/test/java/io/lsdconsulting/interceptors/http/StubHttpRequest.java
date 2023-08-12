package io.lsdconsulting.interceptors.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;

import java.net.URI;

public class StubHttpRequest implements HttpRequest {

    private final String methodValue;
    private final URI uri;
    private final HttpHeaders httpHeaders;

    public StubHttpRequest(String methodValue, URI uri, HttpHeaders httpHeaders) {
        this.methodValue = methodValue;
        this.uri = uri;
        this.httpHeaders = httpHeaders;
    }

    public static StubHttpRequestBuilder builder() {
        return new StubHttpRequestBuilder();
    }

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

    public static class StubHttpRequestBuilder {
        private String methodValue;
        private URI uri;
        private HttpHeaders httpHeaders;

        StubHttpRequestBuilder() {
        }

        public StubHttpRequestBuilder methodValue(String methodValue) {
            this.methodValue = methodValue;
            return this;
        }

        public StubHttpRequestBuilder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        public StubHttpRequestBuilder httpHeaders(HttpHeaders httpHeaders) {
            this.httpHeaders = httpHeaders;
            return this;
        }

        public StubHttpRequest build() {
            return new StubHttpRequest(this.methodValue, this.uri, this.httpHeaders);
        }

        public String toString() {
            return "StubHttpRequest.StubHttpRequestBuilder(methodValue=" + this.methodValue + ", uri=" + this.uri + ", httpHeaders=" + this.httpHeaders + ")";
        }
    }
}