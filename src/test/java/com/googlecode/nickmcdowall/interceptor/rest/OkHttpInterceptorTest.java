package com.googlecode.nickmcdowall.interceptor.rest;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import okhttp3.*;
import okio.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static okhttp3.RequestBody.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OkHttpInterceptorTest {

    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");
    @Mock(lenient = true)
    private Interceptor.Chain chain;

    @Mock
    private TestState interactions = new TestState();

    private Interceptor okHttpInterceptor;
    private String requestBodyString = "{\"name\":\"nick\"}";
    private String url = "https://localhost:8080/user";
    private final String resonpseBodyString = "{}";
    private Response okResponse = anOkResponse(resonpseBodyString);

    @BeforeEach
    void setUp() throws IOException {
        okHttpInterceptor = new OkHttpInterceptor(interactions, "App", Map.of("/user", "UserService"));
        when(chain.request()).thenReturn(aPutRequest(url, requestBodyString));
        when(chain.proceed(any())).thenReturn(okResponse);
    }

    @Test
    void logsRequest() throws IOException {
        okHttpInterceptor.intercept(chain);

        verify(interactions).log("PUT /user from App to UserService", requestBodyString);
    }

    @Test
    void logsResponse() throws IOException {
        okHttpInterceptor.intercept(chain);

        verify(interactions).log("200 OK response from UserService to App", resonpseBodyString);
    }

    @Test
    void requestIsStillIntactAfterIntercept() throws IOException {
        okHttpInterceptor.intercept(chain);

        Buffer buffer = new Buffer();
        chain.request().body().writeTo(buffer);
        assertThat(buffer.readUtf8()).isEqualTo(requestBodyString);
    }

    @Test
    void returnsExpectedResponse() throws IOException {
        Response response = okHttpInterceptor.intercept(chain);

        assertThat(response).isEqualTo(okResponse);
    }

    @Test
    void doesNotCloseResponseBody() throws IOException {
        when(chain.proceed(any())).thenReturn(anOkResponse(resonpseBodyString));

        Response response = okHttpInterceptor.intercept(chain);

        assertThat(response.body().string()).isEqualTo(resonpseBodyString);
    }

    @Test
    void handleUnknownDestinationMapping() throws IOException {
        when(chain.request()).thenReturn(aPutRequest("https://localhost:8080/someOtherPath", requestBodyString));

        okHttpInterceptor.intercept(chain);

        verify(interactions).log("PUT /someOtherPath from App to Other", requestBodyString);
    }

    @Test
    void handlePartialPathMatch() throws IOException {
        when(chain.request()).thenReturn(aPutRequest("https://localhost:8080/user/upload", requestBodyString));

        okHttpInterceptor.intercept(chain);

        verify(interactions).log("PUT /user/upload from App to UserService", requestBodyString);
    }

    private Request aPutRequest(String url, String requestBodyString) {
        return new Request.Builder()
                .url(url)
                .method("PUT", create(MEDIA_TYPE, requestBodyString))
                .build();
    }

    private Response anOkResponse(String body) {
        return new Response.Builder()
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("OK")
                .request(aPutRequest(url, requestBodyString))
                .body(ResponseBody.create(MEDIA_TYPE, body.getBytes()))
                .build();
    }
}
