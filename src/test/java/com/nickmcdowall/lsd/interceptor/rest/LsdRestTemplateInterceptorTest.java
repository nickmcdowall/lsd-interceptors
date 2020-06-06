package com.nickmcdowall.lsd.interceptor.rest;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.rest.StubHttpRequest.StubHttpRequestBuilder;
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

import static com.nickmcdowall.lsd.interceptor.autoconfigure.LsdRestTemplateAutoConfiguration.ALWAYS_APP;
import static com.nickmcdowall.lsd.interceptor.common.UserSuppliedMappings.userSuppliedMappings;
import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.EMPTY;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class LsdRestTemplateInterceptorTest {

    private final URI uri = URI.create("/price/watch");
    private final String requestBodyString = "a request body";
    private final byte[] requestBodyBytes = requestBodyString.getBytes();
    private final StubHttpRequest stubHttpRequest = aGetRequest().build();
    private final String responseBodyString = "a response body";
    private final InputStream responseBodyStream = new ByteArrayInputStream(responseBodyString.getBytes());
    private final ClientHttpResponse httpResponse = aStubbedOkResponse().build();

    @Mock
    private final TestState interactions = new TestState();

    @Mock
    private ClientHttpRequestExecution execution;

    private LsdRestTemplateInterceptor interceptor;

    @BeforeEach
    void setUp() throws IOException {
        interceptor = new LsdRestTemplateInterceptor(interactions, ALWAYS_APP, userSuppliedMappings(of("/price", "PriceService")));
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

        verify(interactions).log("GET " + uri + " from App to PriceService", requestBodyString);
    }

    @Test
    void logResponseInteraction() throws IOException {
        interceptor.intercept(stubHttpRequest, requestBodyBytes, execution);

        verify(interactions).log("200 OK response from PriceService to App", responseBodyString);
    }

    @Test
    void doesNotConsumeResponseStream() throws IOException {
        ClientHttpResponse response = interceptor.intercept(stubHttpRequest, requestBodyBytes, execution);

        assertThat(response.getBody()).hasContent(responseBodyString);
    }

    @Test
    void handleUnknownDestinationMappingByFallingBackToPathNameResolver() throws IOException {
        HttpRequest request = aGetRequest().uri(URI.create("/another/path")).build();

        interceptor.intercept(request, requestBodyBytes, execution);

        verify(interactions).log("GET /another/path from App to another", requestBodyString);
        verify(interactions).log("200 OK response from another to App", responseBodyString);
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

    private StubHttpRequestBuilder aGetRequest() {
        return StubHttpRequest.builder()
                .uri(uri).methodValue("GET").httpHeaders(EMPTY);
    }

}
