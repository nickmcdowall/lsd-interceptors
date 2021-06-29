package io.lsdconsulting.interceptors.http.naming;

import io.lsdconsulting.interceptors.common.AppName;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class AlwaysAppName implements SourceNameMappings {
    AppName appName;
    
    @Override
    public String mapForPath(String path) {
        return appName.getValue();
    }
}
