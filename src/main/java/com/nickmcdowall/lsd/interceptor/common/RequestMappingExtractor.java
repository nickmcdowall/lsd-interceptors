package com.nickmcdowall.lsd.interceptor.common;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class RequestMappingExtractor {

    public static final String PREFIX_UNTIL_VARIABLES = "^(/?.*?)([{?].*|$)";

    public static Set<String> extractPathPrefixes(RequestMappingHandlerMapping handlerMapping) {
        return handlerMapping.getHandlerMethods().keySet().stream()
                .map(requestMappingInfo -> requestMappingInfo.getPatternsCondition().getPatterns())
                .flatMap(Collection::stream)
                .map(s -> s.replaceAll(PREFIX_UNTIL_VARIABLES, "$1"))
                .collect(toSet());
    }
}
