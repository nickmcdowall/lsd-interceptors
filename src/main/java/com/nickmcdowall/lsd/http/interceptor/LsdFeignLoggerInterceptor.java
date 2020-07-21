package com.nickmcdowall.lsd.http.interceptor;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Optional;

import static com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates.requestOf;
import static com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates.responseOf;

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
        String path = derivePathWithoutQueryParameters(request.url());
        String source = sourceNames.mapForPath(path);
        String destination = destinationNames.mapForPath(path);

        testState.log(requestOf(request.httpMethod().name(), path, source, destination), body);
    }

    private void captureResponseInteraction(Response response, String body) {
        String path = derivePathWithoutQueryParameters(response.request().url());
        String source = sourceNames.mapForPath(path);
        String destination = destinationNames.mapForPath(path);

        testState.log(responseOf(deriveStatus(response.status()), destination, source), body);
    }

    private String deriveStatus(int code) {
        Optional<HttpStatus> httpStatus = Optional.ofNullable(HttpStatus.resolve(code));
        return httpStatus.map(HttpStatus::toString)
                .orElse(String.format("<unresolved status:%s>", code));
    }

    private String derivePathWithoutQueryParameters(String url) {
        return url.split("\\?")[0].replaceAll(EXTRACT_PATH, "$1");
    }

    private String extractResponseBodyToString(Response response) throws IOException {
        byte[] bytes = Util.toByteArray(response.body().asInputStream());
        return new String(bytes);
    }

    private Response resetBodyData(Response response, byte[] bodyData) {
        return response.toBuilder().body(bodyData).build();
    }
}
