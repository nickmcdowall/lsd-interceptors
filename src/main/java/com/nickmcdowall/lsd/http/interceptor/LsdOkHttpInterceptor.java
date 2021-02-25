package com.nickmcdowall.lsd.http.interceptor;

import com.nickmcdowall.lsd.http.common.HttpInteractionHandler;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.nickmcdowall.lsd.http.common.Headers.singleValueMap;

@Value
@RequiredArgsConstructor
public class LsdOkHttpInterceptor implements Interceptor {

    public static final int RESPONSE_MAXY_BYTES = 10000;

    private final List<HttpInteractionHandler> handlers;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request requestCopy = request.newBuilder().build();
        String path = request.url().encodedPath();
        Map<String, String> requestHeaders = singleValueMap(request.headers().toMultimap());

        handlers.forEach(handler ->
                handler.handleRequest(request.method(), requestHeaders, path, bodyToString(requestCopy)));

        Response response = chain.proceed(request);
        Map<String, String> responseHeaders = singleValueMap(response.headers().toMultimap());

        handlers.forEach(handler ->
                handler.handleResponse(response.code() + " " + response.message(), responseHeaders, path, copyBodyString(response)));

        return response;
    }

    /*
     * Prevent closing the response body stream by peeking. The max bytes is to preven OOM for ridiculous size bodies
     */
    @SneakyThrows
    private String copyBodyString(Response response) {
        return response.peekBody(RESPONSE_MAXY_BYTES).string();
    }

    @SneakyThrows
    private String bodyToString(Request copy) {
        final Buffer buffer = new Buffer();
        copy.body().writeTo(buffer);
        return buffer.readUtf8();
    }
}
