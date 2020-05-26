package com.googlecode.nickmcdowall.interceptor.rest;

import com.googlecode.nickmcdowall.interceptor.common.PathToDestinationNameMapper;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

import java.io.IOException;

import static com.googlecode.nickmcdowall.interceptor.common.HttpInteractionMessageTemplates.requestOf;
import static com.googlecode.nickmcdowall.interceptor.common.HttpInteractionMessageTemplates.responseOf;

@RequiredArgsConstructor
public class OkHttpInterceptor implements Interceptor {

    public static final int RESPONSE_MAXY_BYTES = 10000;
    private final TestState interactions;
    private final String sourceName;
    private final PathToDestinationNameMapper destinationNames;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request requestCopy = request.newBuilder().build();
        String path = request.url().encodedPath();

        String destinationName = destinationNames.mapForPath(path);

        interactions.log(requestOf(request.method(), path, sourceName, destinationName), bodyToString(requestCopy));

        Response response = chain.proceed(request);

        interactions.log(responseOf(response.code() + " " + response.message(), destinationName, sourceName), copyBodyString(response));

        return response;
    }

    /*
     * Prevent closing the response body stream by peeking. The max bytes is to preven OOM for ridiculous size bodies
     */
    private String copyBodyString(Response response) throws IOException {
        return response.peekBody(RESPONSE_MAXY_BYTES).string();
    }

    private String bodyToString(Request copy) throws IOException {
        final Buffer buffer = new Buffer();
        copy.body().writeTo(buffer);
        return buffer.readUtf8();
    }
}
