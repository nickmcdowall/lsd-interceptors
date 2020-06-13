package com.nickmcdowall.lsd.interceptor.naming;

public interface SourceNameMappings {
    SourceNameMappings ALWAYS_APP = path -> "App";

    String mapForPath(String path);
}
