package com.nickmcdowall.lsd.http.naming;

import lombok.Value;

@Value
public class AppName implements SourceNameMappings {

    private final String value;

    public AppName(String value) {
        this.value = value
                .replaceAll("[()/]", "_")
                .replaceAll(" ", "");
    }

    @Override
    public String mapForPath(String path) {
        return value;
    }
}
