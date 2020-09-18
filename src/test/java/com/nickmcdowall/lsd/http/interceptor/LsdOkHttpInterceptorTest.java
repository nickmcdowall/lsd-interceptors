package com.nickmcdowall.lsd.http.interceptor;

import com.nickmcdowall.lsd.http.common.HttpInteractionHandler;
import okhttp3.*;
import okio.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static okhttp3.RequestBody.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LsdOkHttpInterceptorTest {

    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    private final String requestBodyString = "{\"name\":\"nick\"}";
    private final String responseBodyString = "{}";
    private final Response okResponse = anOkResponse();

    @Mock(lenient = true)
    private Interceptor.Chain chain;

    @Mock
    private HttpInteractionHandler handler;

    private Interceptor okHttpInterceptor;

    @BeforeEach
    void setUp() throws IOException {
        okHttpInterceptor = new LsdOkHttpInterceptor(List.of(handler));
        when(chain.request()).thenReturn(requestFor("PUT", "/user"));
        when(chain.proceed(any())).thenReturn(okResponse);
    }

    @Test
    void delegatesMessageHandling() throws IOException {
        okHttpInterceptor.intercept(chain);

        verify(handler).handleRequest("PUT", "/user", requestBodyString);
        verify(handler).handleResponse("200 OK", "/user", responseBodyString);
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

    private Request requestFor(String method, final String path) {
        return new Request.Builder()
                .url("https://localhost:8080" + path)
                .method(method, create(MEDIA_TYPE, requestBodyString))
                .build();
    }

    private Response anOkResponse() {
        return new Response.Builder()
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("OK")
                .request(requestFor("PUT", "/user"))
                .body(ResponseBody.create(MEDIA_TYPE, responseBodyString.getBytes()))
                .build();
    }
}
