package com.nickmcdowall.lsd.http.interceptor;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

import java.io.IOException;

@Value
@RequiredArgsConstructor
public class LsdOkHttpInterceptor implements Interceptor {

    public static final int RESPONSE_MAXY_BYTES = 10000;

    private final TestState interactions;
    private final SourceNameMappings sourceNames;
    private final DestinationNameMappings destinationNames;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request requestCopy = request.newBuilder().build();
        String path = request.url().encodedPath();

        String destinationName = destinationNames.mapForPath(path);
        String sourceName = sourceNames.mapForPath(path);

        interactions.log(HttpInteractionMessageTemplates.requestOf(request.method(), path, sourceName, destinationName), bodyToString(requestCopy));

        Response response = chain.proceed(request);

        interactions.log(HttpInteractionMessageTemplates.responseOf(response.code() + " " + response.message(), destinationName, sourceName), copyBodyString(response));

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
