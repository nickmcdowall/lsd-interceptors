package com.nickmcdowall.lsd.http.interceptor;

import com.nickmcdowall.lsd.http.common.HttpInteractionHandler;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created to intercept rest template calls for Yatspec interactions.
 * Attempts to reset the input stream so that no data is lost on reading the reponse body
 */
@Value
@RequiredArgsConstructor
public class LsdRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final List<HttpInteractionHandler> handlers;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String path = request.getURI().getPath();
        var requestHeaders = request.getHeaders().toSingleValueMap();
        handlers.forEach(handler ->
                handler.handleRequest(request.getMethodValue(), requestHeaders, path, new String(body)));

        ClientHttpResponse response = execution.execute(request, body);
        handlers.forEach(handler ->
                handler.handleResponse(deriveResponseStatus(response), requestHeaders, path, copyBodyToString(response)));

        return response;
    }

    @SneakyThrows
    private String deriveResponseStatus(ClientHttpResponse response) {
        return response.getStatusCode().toString();
    }

    @SneakyThrows
    private String copyBodyToString(ClientHttpResponse response) {
        if (response.getHeaders().getContentLength() == 0)
            return "";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = response.getBody();
        inputStream.transferTo(outputStream);
        return outputStream.toString();
    }

}
