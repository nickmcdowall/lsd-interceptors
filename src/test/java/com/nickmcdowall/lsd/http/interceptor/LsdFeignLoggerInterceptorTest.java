package com.nickmcdowall.lsd.http.interceptor;

import com.nickmcdowall.lsd.http.common.HttpInteractionHandler;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * protected methods so extending to gain access
 */
@ExtendWith(MockitoExtension.class)
public class LsdFeignLoggerInterceptorTest extends LsdFeignLoggerInterceptor {

    private final Map<String, Collection<String>> headers = emptyMap();
    private final Level level = Level.BASIC;

    public LsdFeignLoggerInterceptorTest() {
        super(List.of(mock(HttpInteractionHandler.class)));
    }

    @Test
    void logsRequest() {
        logRequest("configKey", level, requestWithBody("body"));

        handlers.forEach(handler -> {
            verify(handler).handleRequest("GET", "/app-endpoint/something", "body");
        });
    }

    @Test
    void handlesEmptyBody() {
        logRequest("configKey", level, requestWithBody(null));

        handlers.forEach(handler -> {
            verify(handler).handleRequest("GET", "/app-endpoint/something", "");
        });
    }

    @Test
    void handlesPathParameters() {
        logRequest("configKey", level, requestWithParameter("/app-endpoint/something?someParam=hi&secondParam=yo"));

        handlers.forEach(handler -> {
            verify(handler).handleRequest("GET", "/app-endpoint/something?someParam=hi&secondParam=yo", "");
        });
    }

    @Test
    void capturesResponseInteraction() throws IOException {
        logAndRebufferResponse("configKey", level, Response.builder()
                .request(requestWithBody("body"))
                .body("response body".getBytes())
                .status(200)
                .build(), 1);

        handlers.forEach(handler -> {
            verify(handler).handleResponse("200 OK", "/app-endpoint/something", "response body");
        });
    }

    @Test
    void handleHttpRequest() throws IOException {
        logAndRebufferResponse("configKey", level, Response.builder()
                .request(requestWithBody("body", "http"))
                .body("response body".getBytes())
                .status(200)
                .build(), 1);

        handlers.forEach(handler -> {
            verify(handler).handleResponse("200 OK", "/app-endpoint/something", "response body");
        });
    }

    @Test
    void handlesUnresolvedStatusCode() throws IOException {
        logAndRebufferResponse("configKey", level, Response.builder()
                .request(requestWithBody("body"))
                .body("response body".getBytes())
                .status(111)
                .build(), 1);

        handlers.forEach(handler -> {
            verify(handler).handleResponse("<unresolved status:111>", "/app-endpoint/something", "response body");
        });
    }

    @Test
    void preservesResponseStream() throws IOException {
        Response response = logAndRebufferResponse("configKey", level, Response.builder()
                .request(requestWithBody("request body"))
                .body("response body".getBytes())
                .status(200)
                .build(), 1);

        assertThat(response.body().asInputStream()).hasContent("response body");
    }

    private Request requestWithBody(String body) {
        return Request.create(
                GET,
                "https://localhost:8080/app-endpoint/something",
                headers, null == body ? null : body.getBytes(),
                defaultCharset(),
                new RequestTemplate());
    }

    private Request requestWithBody(String body, final String protocol) {
        return Request.create(
                GET,
                protocol + "://localhost:8080/app-endpoint/something",
                headers, null == body ? null : body.getBytes(),
                defaultCharset(),
                new RequestTemplate());
    }

    private Request requestWithParameter(final String pathWithParam) {
        return Request.create(
                GET,
                "https://localhost:8080" + pathWithParam,
                headers, null,
                defaultCharset(),
                new RequestTemplate());
    }
}
