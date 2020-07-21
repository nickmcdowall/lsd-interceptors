package com.nickmcdowall.lsd.http.interceptor;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.naming.UserSuppliedDestinationMappings;
import com.nickmcdowall.lsd.http.naming.UserSuppliedSourceMappings;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.emptyMap;
import static java.util.Map.of;
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
        super(mock(TestState.class),
                UserSuppliedSourceMappings.userSuppliedSourceMappings(of("/app-endpoint", "User")),
                UserSuppliedDestinationMappings.userSuppliedDestinationMappings(of("/app-endpoint", "App"))
        );
    }

    @Test
    void logsRequest() {
        logRequest("configKey", level, requestWithBody("body"));

        verify(testState).log("GET /app-endpoint/something from User to App", "body");
    }

    @Test
    void handlesEmptyBody() {
        logRequest("configKey", level, requestWithBody(null));

        verify(testState).log("GET /app-endpoint/something from User to App", "");
    }

    @Test
    void handlesPathParameters() {
        logRequest("configKey", level, requestWithParameter("/app-endpoint/something?someParam=hi,secondParam=yo"));

        verify(testState).log("GET /app-endpoint/something from User to App", "");
    }

    @Test
    void capturesResponseInteraction() throws IOException {
        logAndRebufferResponse("configKey", level, Response.builder()
                .request(requestWithBody("body"))
                .body("response body".getBytes())
                .status(200)
                .build(), 1);

        verify(testState).log("200 OK response from App to User", "response body");
    }

    @Test
    void handleHttpRequest() throws IOException {
        logAndRebufferResponse("configKey", level, Response.builder()
                .request(requestWithBody("body", "http"))
                .body("response body".getBytes())
                .status(200)
                .build(), 1);

        verify(testState).log("200 OK response from App to User", "response body");
    }

    @Test
    void handlesUnresolvedStatusCode() throws IOException {
        logAndRebufferResponse("configKey", level, Response.builder()
                .request(requestWithBody("body"))
                .body("response body".getBytes())
                .status(111)
                .build(), 1);

        verify(testState).log("<unresolved status:111> response from App to User", "response body");
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
