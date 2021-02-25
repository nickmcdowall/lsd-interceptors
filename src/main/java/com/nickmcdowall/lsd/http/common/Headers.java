package com.nickmcdowall.lsd.http.common;

import java.util.Collection;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class Headers {

    public enum HeaderKeys {
        SOURCE_SERVICE_NAME("SourceServiceName"),
        TARGET_SERVICE_NAME("TargetServiceName");

        private String headerName;

        HeaderKeys(String headerName) {
            this.headerName = headerName;
        }

        public String key() {
            return headerName;
        }
    }

    public static Map<String, String> singleValueMap(Map<String, ? extends Collection<String>> headers) {
        return headers.entrySet().stream()
                .collect(toMap(entry -> entry.getKey(), entry -> entry.getValue().stream()
                        .findFirst()
                        .orElse("")));
    }
}
