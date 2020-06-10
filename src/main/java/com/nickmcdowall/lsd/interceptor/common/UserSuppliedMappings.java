package com.nickmcdowall.lsd.interceptor.common;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Map;

import static java.util.Comparator.reverseOrder;

@Value
@RequiredArgsConstructor
public class UserSuppliedMappings implements PathToNameMapper {

    private final Map<String, String> mappings;
    private final PathToNameMapper fallbackMapper;

    @Override
    public String mapForPath(String path) {
        String nameKey = mappings.keySet().stream()
                .sorted(reverseOrder())
                .filter(path::startsWith)
                .findFirst()
                .orElse("default");

        return mappings.getOrDefault(nameKey, fallbackMapper.mapForPath(path));
    }

    public static UserSuppliedMappings userSuppliedMappings(Map<String, String> mappings) {
        return new UserSuppliedMappings(mappings, new RegexResolvingNameMapper());
    }

}
