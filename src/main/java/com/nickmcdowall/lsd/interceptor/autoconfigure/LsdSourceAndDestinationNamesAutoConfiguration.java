package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.interceptor.naming.SourceNameMappings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;
import java.util.Set;

import static com.nickmcdowall.lsd.interceptor.naming.UserSuppliedDestinationMappings.userSuppliedDestinationMappings;
import static com.nickmcdowall.lsd.interceptor.naming.UserSuppliedSourceMappings.userSuppliedSourceMappings;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * <p>
 * If a {@link TestState} and a {@link RequestMappingHandlerMapping} bean is available it will automatically autoconfig
 * a source and destination name mappings using the path prefixes contained in the {@link RequestMappingHandlerMapping} bean.
 * </p>
 * <br/>
 */
@Configuration
@ConditionalOnBean(value = {TestState.class, RequestMappingHandlerMapping.class})
class LsdSourceAndDestinationNamesAutoConfiguration {

    public static final String PREFIX_UNTIL_VARIABLES = "^(/?.*?)([{?].*|$)";

    @Bean
    @ConditionalOnMissingBean(name = "defaultSourceNameMapping")
    public SourceNameMappings defaultSourceNameMapping(final RequestMappingHandlerMapping handlerMapping) {
        return userSuppliedSourceMappings(extractPathPrefixes(handlerMapping).stream()
                .collect(toMap(path -> path, path -> "User")));
    }

    @Bean
    @ConditionalOnMissingBean(name = "defaultDestinationNameMapping")
    public DestinationNameMappings defaultDestinationNameMapping(final RequestMappingHandlerMapping handlerMapping) {
        return userSuppliedDestinationMappings(
                extractPathPrefixes(handlerMapping).stream()
                        .collect(toMap(path -> path, path -> "App"))
        );
    }

    private Set<String> extractPathPrefixes(RequestMappingHandlerMapping handlerMapping) {
        return handlerMapping.getHandlerMethods().keySet().stream()
                .map(requestMappingInfo -> requestMappingInfo.getPatternsCondition().getPatterns())
                .flatMap(Collection::stream)
                .map(s -> s.replaceAll(PREFIX_UNTIL_VARIABLES, "$1"))
                .collect(toSet());
    }

}
