package com.nickmcdowall.lsd.http.naming;

import lombok.Value;

@Value
public class AppName implements SourceNameMappings {

    private final String appName;

    public AppName(String appName) {
        this.appName = appName.replaceAll("[( )/]", "_");
    }

    @Override
    public String mapForPath(String path) {
        return appName;
    }
}
