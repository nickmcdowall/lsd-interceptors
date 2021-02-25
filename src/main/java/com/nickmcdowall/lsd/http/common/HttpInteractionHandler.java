package com.nickmcdowall.lsd.http.common;

import java.util.Map;

public interface HttpInteractionHandler {
    void handleRequest(String method, Map<String, String> headers, String path, String body);

    void handleResponse(String statusMessage, Map<String, String> headers, String path, String body);
}
