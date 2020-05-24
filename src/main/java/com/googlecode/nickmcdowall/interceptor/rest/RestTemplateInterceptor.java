package com.googlecode.nickmcdowall.interceptor.rest;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static java.lang.String.format;

/**
 * Created to intercept rest template calls for Yatspec interactions.
 * Attempts to reset the input stream so that no data is lost on reading the reponse body
 */
@RequiredArgsConstructor
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    public static final String REQUEST_TEMPLATE = "%s from %s to %s";
    public static final String RESPONSE_TEMPLATE = "%s response from %s to %s";

    private final TestState interactions;
    private final String sourceName;
    private final Map<String, String> destinationMapping;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String path = request.getURI().getPath();
        String destinationName = determineDestinationName(path);

        captureRequest(request, body, path, destinationName);
        ClientHttpResponse response = execution.execute(request, body);
        captureResponse(destinationName, response);

        return response;
    }

    private void captureRequest(HttpRequest request, byte[] body, String path, String destinationName) {
        interactions.log(format(REQUEST_TEMPLATE, request.getMethodValue() + " " + path, sourceName, destinationName), body);
    }

    private void captureResponse(String destinationName, ClientHttpResponse response) throws IOException {
        interactions.log(format(RESPONSE_TEMPLATE, response.getStatusCode(), destinationName, sourceName), copyBodyToString(response));
    }

    private String copyBodyToString(ClientHttpResponse response) throws IOException {
        if (response.getHeaders().getContentLength() == 0)
            return "";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = response.getBody();
        inputStream.transferTo(outputStream);
        inputStream.reset();
        return outputStream.toString();
    }

    private String determineDestinationName(String path) {
        return destinationMapping.getOrDefault(destinationNameKey(path), "Other");
    }

    private String destinationNameKey(String path) {
        return destinationMapping.keySet().stream()
                .filter(key -> path.startsWith(key))
                .findFirst()
                .orElse("");
    }
}
