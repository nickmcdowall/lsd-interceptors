package com.nickmcdowall.lsd.interceptor.common;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import static java.util.Comparator.reverseOrder;

@RequiredArgsConstructor
public class UserSuppliedMappings implements DestinationNamesMapper {

    private final Map<String, String> destinationNames;

    @Override
    public String mapForPath(String path) {
        String nameKey = destinationNames.keySet().stream()
                .sorted(reverseOrder())
                .filter(path::startsWith)
                .findFirst()
                .orElse("default");

        return destinationNames.getOrDefault(nameKey, "Other");
    }

    public static UserSuppliedMappings userSuppliedMappings(Map<String, String> destinationMapping) {
        return new UserSuppliedMappings(destinationMapping);
    }

}
