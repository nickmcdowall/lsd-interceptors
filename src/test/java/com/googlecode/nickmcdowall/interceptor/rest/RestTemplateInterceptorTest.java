package com.googlecode.nickmcdowall.interceptor.rest;

import com.googlecode.nickmcdowall.interceptor.rest.StubHttpRequest.StubHttpRequestBuilder;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.EMPTY;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class RestTemplateInterceptorTest {

    private final String methodValue = "GET";
    private final URI uri = URI.create("/price/watch");
    private final byte[] requestBodyBytes = toBytes("a request body");
    private final StubHttpRequest stubHttpRequest = aStubbedRequest().build();

    private final String responseBodyString = "a response body";
    private final InputStream responseBodyStream = new ByteArrayInputStream(responseBodyString.getBytes());
    private final ClientHttpResponse httpResponse = aStubbedOkResponse().build();

    @Mock
    private TestState interactions = new TestState();

    @Mock
    private ClientHttpRequestExecution execution;

    private RestTemplateInterceptor interceptor;

    @BeforeEach
    void setUp() throws IOException {
        interceptor = new RestTemplateInterceptor(interactions, "App", Map.of("/price", "PriceService"));
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
        HttpRequest request = stubHttpRequest;

        ClientHttpResponse interceptedResponse = interceptor.intercept(request, requestBodyBytes, execution);

        assertThat(interceptedResponse).isEqualTo(httpResponse);
    }

    @Test
    void logRequestInteraction() throws IOException {
        HttpRequest request = stubHttpRequest;

        interceptor.intercept(request, requestBodyBytes, execution);

        verify(interactions).log("GET " + uri + " from App to PriceService", requestBodyBytes);
    }

    @Test
    void logResponseInteraction() throws IOException {
        HttpRequest request = stubHttpRequest;

        interceptor.intercept(request, requestBodyBytes, execution);

        verify(interactions).log("200 OK response from PriceService to App", responseBodyString);
    }

    @Test
    void doesNotConsumeResponseStream() throws IOException {
        HttpRequest request = stubHttpRequest;

        ClientHttpResponse response = interceptor.intercept(request, requestBodyBytes, execution);

        assertThat(response.getBody()).hasContent(responseBodyString);
    }

    @Test
    void handleUnknownDestinationMapping() throws IOException {
        HttpRequest request = aStubbedRequest().uri(URI.create("/another/path")).build();

        interceptor.intercept(request, requestBodyBytes, execution);

        verify(interactions).log("GET /another/path from App to Other", requestBodyBytes);
        verify(interactions).log("200 OK response from Other to App", responseBodyString);
    }

    @Test
    void emptyStringOnZeroLengthResponse() throws IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentLength(0);

        when(execution.execute(any(), any())).thenReturn(aStubbedOkResponse().headers(httpHeaders).build());

        interceptor.intercept(stubHttpRequest, requestBodyBytes, execution);

        verify(interactions).log("200 OK response from PriceService to App", "");
    }

    private StubClientHttpResponse.StubClientHttpResponseBuilder aStubbedOkResponse() {
        return StubClientHttpResponse.builder()
                .body(responseBodyStream)
                .statusCode(OK)
                .headers(EMPTY);
    }

    private StubHttpRequestBuilder aStubbedRequest() {
        return StubHttpRequest.builder()
                .uri(uri).methodValue(methodValue).httpHeaders(EMPTY);
    }

    private byte[] toBytes(String body) {
        return body.getBytes();
    }

}
