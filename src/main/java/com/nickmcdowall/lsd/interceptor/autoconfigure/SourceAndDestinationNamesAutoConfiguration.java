package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.PathToNameMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static com.nickmcdowall.lsd.interceptor.common.RequestMappingExtractor.extractPathPrefixes;
import static com.nickmcdowall.lsd.interceptor.common.UserSuppliedMappings.userSuppliedMappings;
import static java.util.stream.Collectors.toMap;

/**
 * <p>
 * If a {@link TestState} and a {@link RequestMappingHandlerMapping} bean is available it will automatically autoconfig
 * a source and destination name mappings using the path prefixes contained in the {@link RequestMappingHandlerMapping} bean.
 * </p>
 * <br/>
 */
@Configuration
@ConditionalOnBean(value = {TestState.class, RequestMappingHandlerMapping.class})
class SourceAndDestinationNamesAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "defaultSourceNameMapping")
    public PathToNameMapper defaultSourceNameMapping(final RequestMappingHandlerMapping handlerMapping) {
        return mapMatchingPathToName(handlerMapping, "User");
    }

    @Bean
    @ConditionalOnMissingBean(name = "defaultDestinationNameMapping")
    public PathToNameMapper defaultDestinationNameMapping(final RequestMappingHandlerMapping handlerMapping) {
        return mapMatchingPathToName(handlerMapping, "App");
    }

    private PathToNameMapper mapMatchingPathToName(RequestMappingHandlerMapping handlerMapping, String name) {
        return userSuppliedMappings(extractPathPrefixes(handlerMapping).stream()
                .collect(toMap(path -> path, path -> name)));
    }
}
