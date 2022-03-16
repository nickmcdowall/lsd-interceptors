package io.lsdconsulting.interceptors.http;

import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler;
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
 * Created to intercept rest template calls for lsd interactions.
 * Attempts to reset the input stream so that no data is lost on reading the reponse body
 */
@Value
@RequiredArgsConstructor
public class LsdRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    List<HttpInteractionHandler> handlers;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        var path = request.getURI().toString();
        var requestHeaders = request.getHeaders().toSingleValueMap();
        handlers.forEach(handler ->
                handler.handleRequest(request.getMethodValue(), requestHeaders, path, new String(body)));

        var response = execution.execute(request, body);
        var responseHeaders = response.getHeaders().toSingleValueMap();
        handlers.forEach(handler ->
                handler.handleResponse(deriveResponseStatus(response), requestHeaders, responseHeaders, path, copyBodyToString(response)));

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
