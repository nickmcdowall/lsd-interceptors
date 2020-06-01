package com.nickmcdowall.lsd.interceptor.rest;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.DestinationNamesMapper;
import com.nickmcdowall.lsd.interceptor.common.HttpInteractionMessageTemplates;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created to intercept rest template calls for Yatspec interactions.
 * Attempts to reset the input stream so that no data is lost on reading the reponse body
 */
@RequiredArgsConstructor
@Value
public class LsdRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final TestState interactions;
    private final String sourceName;
    private final DestinationNamesMapper destinationNameMappings;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String path = request.getURI().getPath();
        String destinationName = destinationNameMappings.mapForPath(path);

        captureRequest(request, body, path, destinationName);
        ClientHttpResponse response = execution.execute(request, body);
        captureResponse(destinationName, response);

        return response;
    }

    private void captureRequest(HttpRequest request, byte[] body, String path, String destinationName) {
        String interactionMessage = HttpInteractionMessageTemplates.requestOf(request.getMethodValue(), path, sourceName, destinationName);
        interactions.log(interactionMessage, new String(body));
    }

    private void captureResponse(String destinationName, ClientHttpResponse response) throws IOException {
        String interactionMessage = HttpInteractionMessageTemplates.responseOf(response.getStatusCode().toString(), destinationName, sourceName);
        interactions.log(interactionMessage, copyBodyToString(response));
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

}
