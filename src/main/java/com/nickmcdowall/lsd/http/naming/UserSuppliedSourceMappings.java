package com.nickmcdowall.lsd.http.naming;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Map;

import static java.util.Comparator.reverseOrder;

@Value
@RequiredArgsConstructor
public class UserSuppliedSourceMappings implements SourceNameMappings {

    private final Map<String, String> mappings;
    private final SourceNameMappings fallbackMapper;

    @Override
    public String mapForPath(String path) {
        String nameKey = mappings.keySet().stream()
                .sorted(reverseOrder())
                .filter(path::startsWith)
                .findFirst()
                .orElse("default");

        return mappings.getOrDefault(nameKey, fallbackMapper.mapForPath(path));
    }

    public static SourceNameMappings userSuppliedSourceMappings(Map<String, String> mappings) {
        return new UserSuppliedSourceMappings(mappings, ALWAYS_APP);
    }

}
