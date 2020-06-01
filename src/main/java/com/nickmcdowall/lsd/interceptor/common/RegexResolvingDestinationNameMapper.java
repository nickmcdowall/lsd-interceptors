package com.nickmcdowall.lsd.interceptor.common;

import lombok.Value;

@Value
public class RegexResolvingDestinationNameMapper implements DestinationNamesMapper {

    public static final String FIRST_PART_OF_PATH = "^/?(.*?)([/?].*|$)";

    @Override
    public String mapForPath(String path) {
        return path.replaceFirst(FIRST_PART_OF_PATH, "$1");
    }
}
