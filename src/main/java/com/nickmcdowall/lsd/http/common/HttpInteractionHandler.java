package com.nickmcdowall.lsd.http.common;

public interface HttpInteractionHandler {
    void handleRequest(String method, String path, String body);

    void handleResponse(String statusMessage, String path, String body);
}
