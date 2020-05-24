package com.googlecode.nickmcdowall.interceptor.rest;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestTemplateInterceptorTest {

    public static final String METHOD_VALUE = "GET";
    @Mock
    private TestState interactions = new TestState();

    @Mock
    private ClientHttpRequestExecution execution;

    private URI uri = URI.create("/price/watch");
    private byte[] requestBodyBytes = toBytes("a request body");
    private final String responseBodyString = "a response body";
    private InputStream responseBodyStream = new ByteArrayInputStream(responseBodyString.getBytes());
    private RestTemplateInterceptor interceptor;
    private ClientHttpResponse httpResponse = aStubOkResponse(responseBodyStream);

    @BeforeEach
    void setUp() throws IOException {
        interceptor = new RestTemplateInterceptor(interactions, "App", Map.of("/price", "PriceService"));
        when(execution.execute(any(), any())).thenReturn(httpResponse);
    }

    @Test
    void passActualRequestToExecutor() throws IOException {
        HttpRequest request = aStubRequest(METHOD_VALUE, uri);

        interceptor.intercept(request, requestBodyBytes, execution);

        verify(execution).execute(request, requestBodyBytes);
    }

    @Test
    void returnsActualResponse() throws IOException {
        HttpRequest request = aStubRequest(METHOD_VALUE, uri);

        ClientHttpResponse interceptedResponse = interceptor.intercept(request, requestBodyBytes, execution);

        assertThat(interceptedResponse).isEqualTo(httpResponse);
    }

    @Test
    void logRequestInteraction() throws IOException {
        HttpRequest request = aStubRequest(METHOD_VALUE, uri);

        interceptor.intercept(request, requestBodyBytes, execution);

        verify(interactions).log("GET " + uri + " from App to PriceService", requestBodyBytes);
    }

    @Test
    void logResponseInteraction() throws IOException {
        HttpRequest request = aStubRequest(METHOD_VALUE, uri);

        interceptor.intercept(request, requestBodyBytes, execution);

        verify(interactions).log("200 OK response from PriceService to App", responseBodyString);
    }

    @Test
    void doesNotConsumeResponseStream() throws IOException {
        HttpRequest request = aStubRequest(METHOD_VALUE, uri);

        ClientHttpResponse response = interceptor.intercept(request, requestBodyBytes, execution);

        assertThat(response.getBody()).hasContent(responseBodyString);
    }

    @Test
    void handleUnknownDestinationMapping() throws IOException {
        HttpRequest request = aStubRequest(METHOD_VALUE, URI.create("/another/path"));

        interceptor.intercept(request, requestBodyBytes, execution);

        verify(interactions).log("GET /another/path from App to Other", requestBodyBytes);
        verify(interactions).log("200 OK response from Other to App", responseBodyString);
    }

    private byte[] toBytes(String body) {
        return body.getBytes();
    }

    private ClientHttpResponse aStubOkResponse(final InputStream body) {
        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() {
                return HttpStatus.OK;
            }

            @Override
            public int getRawStatusCode() {
                return 200;
            }

            @Override
            public String getStatusText() {
                return "200 OK";
            }

            @Override
            public void close() {
            }

            @Override
            public InputStream getBody() {
                return body;
            }

            @Override
            public HttpHeaders getHeaders() {
                return HttpHeaders.EMPTY;
            }
        };
    }

    private HttpRequest aStubRequest(final String methodValue, final URI uri) {
        return new HttpRequest() {
            @Override
            public HttpHeaders getHeaders() {
                return HttpHeaders.EMPTY;
            }

            @Override
            public String getMethodValue() {
                return methodValue;
            }

            @Override
            public URI getURI() {
                return uri;
            }
        };
    }
}
