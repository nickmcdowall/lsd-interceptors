package com.nickmcdowall.lsd.http.naming;

public interface SourceNameMappings {
    SourceNameMappings ALWAYS_APP = path -> "App";

    String mapForPath(String path);
}
