package com.googlecode.nickmcdowall.interceptor.common;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class PathToDestinationNameMapper {

    private final Map<String, String> destinationNames;

    public String mapForPath(String path) {
        String nameKey = destinationNames.keySet().stream()
                .filter(path::startsWith)
                .findFirst()
                .orElse("default");

        return destinationNames.getOrDefault(nameKey, "Other");
    }

    public static PathToDestinationNameMapper destinationMappings(Map<String, String> destinationMapping) {
        return new PathToDestinationNameMapper(destinationMapping);
    }

}
