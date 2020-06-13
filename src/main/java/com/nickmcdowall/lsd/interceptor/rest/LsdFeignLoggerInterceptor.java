package com.nickmcdowall.lsd.interceptor.rest;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.interceptor.naming.SourceNameMappings;
import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Optional;

/**
 * Intercepts Feign {@link Request} and {@link Response} messages to add them to the {@link TestState} bean.
 * <p>
 * (This allows yatspec-lsd to display them on the sequence diagrams).
 */
public class LsdFeignLoggerInterceptor extends Logger.JavaLogger {
    public static final String EXTRACT_PATH = "https?://.*?(/.*)";

    protected final TestState testState;
    protected final SourceNameMappings sourceNames;
    protected final DestinationNameMappings destinationNames;

    public LsdFeignLoggerInterceptor(TestState testState, SourceNameMappings sourceNames, DestinationNameMappings destinationNames) {
        super(LsdFeignLoggerInterceptor.class);
        this.testState = testState;
        this.sourceNames = sourceNames;
        this.destinationNames = destinationNames;
    }

    @Override
    protected void logRequest(String configKey, Level level, Request request) {
        super.logRequest(configKey, level, request);
        captureRequestInteraction(request);
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {
        super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
        String body = extractResponseBodyToString(response);
        captureResponseInteraction(response, body);
        return resetBodyData(response, body.getBytes());
    }

    private void captureRequestInteraction(Request request) {
        Optional<byte[]> bodyData = Optional.ofNullable(request.body());
        String body = bodyData.map(String::new).orElse("");
        String path = derivePath(request.url());
        String source = sourceNames.mapForPath(path);
        String destination = destinationNames.mapForPath(path);
        testState.log(request.httpMethod().name() + " " + path + " from " + source + " to " + destination, body);
    }

    private void captureResponseInteraction(Response response, String body) {
        String path = derivePath(response.request().url());
        String source = sourceNames.mapForPath(path);
        String destination = destinationNames.mapForPath(path);
        testState.log(deriveStatus(response.status()) + " response from " + destination + " to " + source, body);
    }

    private String deriveStatus(int code) {
        Optional<HttpStatus> httpStatus = Optional.ofNullable(HttpStatus.resolve(code));
        return httpStatus.map(HttpStatus::toString)
                .orElse(String.format("<unresolved status:%s>", code));
    }

    private String derivePath(String url) {
        return url.replaceAll(EXTRACT_PATH, "$1");
    }

    private String extractResponseBodyToString(Response response) throws IOException {
        byte[] bytes = Util.toByteArray(response.body().asInputStream());
        return new String(bytes);
    }

    private Response resetBodyData(Response response, byte[] bodyData) {
        return response.toBuilder().body(bodyData).build();
    }
}
