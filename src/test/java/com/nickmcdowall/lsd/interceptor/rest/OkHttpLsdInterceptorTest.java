package com.nickmcdowall.lsd.interceptor.rest;

import com.nickmcdowall.lsd.interceptor.common.UserSuppliedMappings;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import okhttp3.*;
import okio.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static java.util.Map.of;
import static okhttp3.RequestBody.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OkHttpLsdInterceptorTest {

    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    private final String requestBodyString = "{\"name\":\"nick\"}";
    private final String responseBodyString = "{}";
    private final Response okResponse = anOkResponse();

    @Mock(lenient = true)
    private Interceptor.Chain chain;

    @Mock
    private TestState interactions;

    private Interceptor okHttpInterceptor;

    @BeforeEach
    void setUp() throws IOException {
        okHttpInterceptor = new OkHttpLsdInterceptor(interactions, "App", new UserSuppliedMappings(of("/user", "UserService")));
        when(chain.request()).thenReturn(aPutRequest());
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

        verify(interactions).log("200 OK response from UserService to App", responseBodyString);
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
        when(chain.proceed(any())).thenReturn(anOkResponse());

        Response response = okHttpInterceptor.intercept(chain);

        assertThat(response.body().string()).isEqualTo(responseBodyString);
    }

    private Request aPutRequest() {
        return new Request.Builder()
                .url("https://localhost:8080/user")
                .method("PUT", create(MEDIA_TYPE, requestBodyString))
                .build();
    }

    private Response anOkResponse() {
        return new Response.Builder()
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("OK")
                .request(aPutRequest())
                .body(ResponseBody.create(MEDIA_TYPE, responseBodyString.getBytes()))
                .build();
    }
}
