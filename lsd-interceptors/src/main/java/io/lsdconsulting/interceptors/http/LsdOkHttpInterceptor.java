package io.lsdconsulting.interceptors.http;

import io.lsdconsulting.interceptors.http.common.Headers;
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

import java.io.IOException;
import java.util.List;

@Value
@RequiredArgsConstructor
public class LsdOkHttpInterceptor implements Interceptor {

    public static final int RESPONSE_MAXY_BYTES = 10000;

    List<HttpInteractionHandler> handlers;

    @Override
    public Response intercept(Chain chain) throws IOException {
        var request = chain.request();
        var requestCopy = request.newBuilder().build();
        var path = request.url().encodedPath();
        var requestHeaders = Headers.singleValueMap(request.headers().toMultimap());

        handlers.forEach(handler ->
                handler.handleRequest(request.method(), requestHeaders, path, bodyToString(requestCopy)));

        var response = chain.proceed(request);
        var responseHeaders = Headers.singleValueMap(response.headers().toMultimap());

        handlers.forEach(handler ->
                handler.handleResponse(response.code() + " " + response.message(), requestHeaders, responseHeaders, path, copyBodyString(response)));

        return response;
    }

    /*
     * Prevent closing the response body stream by peeking. The max bytes is to prevent OOM for ridiculous size bodies
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
