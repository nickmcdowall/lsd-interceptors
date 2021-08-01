package io.lsdconsulting.interceptors.http;

import io.lsdconsulting.interceptors.http.StubHttpRequest.StubHttpRequestBuilder;
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.EMPTY;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
public class LsdRestTemplateInterceptorTest {

    private final String path = "/price/watch";
    private final URI uri = URI.create(path);
    private final String requestBodyString = "a request body";
    private final byte[] requestBodyBytes = requestBodyString.getBytes();
    private final StubHttpRequest stubHttpRequest = aGetRequest(uri).build();
    private final String responseBodyString = "a response body";
    private final InputStream responseBodyStream = new ByteArrayInputStream(responseBodyString.getBytes());
    private final ClientHttpResponse httpResponse = aStubbedOkResponse().build();

    @Mock
    private ClientHttpRequestExecution execution;

    @Mock
    private HttpInteractionHandler handler;

    private LsdRestTemplateInterceptor interceptor;

    @BeforeEach
    void setUp() throws IOException {
        interceptor = new LsdRestTemplateInterceptor(List.of(handler));
        when(execution.execute(any(), any())).thenReturn(httpResponse);
    }

    @Test
    void passActualRequestToExecutor() throws IOException {
        HttpRequest request = stubHttpRequest;

        interceptor.intercept(request, requestBodyBytes, execution);

        verify(execution).execute(request, requestBodyBytes);
    }

    @Test
    void returnsActualResponse() throws IOException {
        ClientHttpResponse interceptedResponse = interceptor.intercept(stubHttpRequest, requestBodyBytes, execution);

        assertThat(interceptedResponse).isEqualTo(httpResponse);
    }

    @Test
    void logRequestInteraction() throws IOException {
        interceptor.intercept(stubHttpRequest, requestBodyBytes, execution);

        verify(handler).handleRequest("GET", emptyMap(), path, requestBodyString);
    }

    @Test
    void logResponseInteraction() throws IOException {
        interceptor.intercept(stubHttpRequest, requestBodyBytes, execution);

        verify(handler).handleResponse("200 OK", emptyMap(), emptyMap(), path, responseBodyString);
    }

    @Test
    void handleUnknownDestinationMappingByFallingBackToPathNameResolver() throws IOException {
        HttpRequest request = aGetRequest(uri).uri(URI.create("/another/path")).build();

        interceptor.intercept(request, requestBodyBytes, execution);

        verify(handler).handleRequest("GET", emptyMap(), "/another/path", requestBodyString);
        verify(handler).handleResponse("200 OK", emptyMap(), emptyMap(), "/another/path", responseBodyString);
    }

    @Test
    void emptyStringOnZeroLengthResponse() throws IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentLength(0);

        when(execution.execute(any(), any())).thenReturn(aStubbedOkResponse().headers(httpHeaders).build());

        interceptor.intercept(stubHttpRequest, requestBodyBytes, execution);

        verify(handler).handleResponse("200 OK", emptyMap(), Map.of("Content-Length", "0"), "/price/watch", "");
    }

    @Test
    void removesPathParametersFromUri() throws IOException {
        interceptor.intercept(aGetRequest(URI.create("/cow?param=yes")).build(), requestBodyBytes, execution);

        verify(handler).handleRequest("GET", emptyMap(), "/cow", requestBodyString);
        verify(handler).handleResponse("200 OK", emptyMap(), emptyMap(), "/cow", responseBodyString);
    }

    private StubClientHttpResponse.StubClientHttpResponseBuilder aStubbedOkResponse() {
        return StubClientHttpResponse.builder()
                .body(responseBodyStream)
                .statusCode(OK)
                .headers(EMPTY);
    }

    private StubHttpRequestBuilder aGetRequest(URI uri) {
        return StubHttpRequest.builder()
                .uri(uri).methodValue("GET").httpHeaders(EMPTY);
    }
}