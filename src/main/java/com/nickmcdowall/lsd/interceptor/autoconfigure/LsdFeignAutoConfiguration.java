package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.PathToNameMapper;
import com.nickmcdowall.lsd.interceptor.common.RegexResolvingNameMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdFeignLoggerInterceptor;
import feign.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * If a {@link TestState} bean is available it will automatically autoconfig
 * a {@link LsdFeignLoggerInterceptor}
 * </p>
 * <br/>
 * <p>
 * It is assumed that if a {@link LsdFeignLoggerInterceptor} bean exists is will be used to invoke downstream endpoints from within the app.
 * Therefore the <em>source</em> name will default to <em>'App'</em> and the <em>destination</em> name will be derived using a
 * {@link RegexResolvingNameMapper} by default.
 * </p>
 * <br/>
 * <p>
 * Users can override the default name mappings by supplying their own {@link PathToNameMapper} beans and calling it
 * <em>'defaultSourceNameMapping`</em> for source names and <em>'defaultDestinationNameMapping`</em>
 * for destination names.
 * </p>
 */
@Configuration
@ConditionalOnBean(value = {TestState.class})
@ConditionalOnClass(value = {FeignClientBuilder.class, Logger.Level.class})
@AutoConfigureAfter(SourceAndDestinationNamesAutoConfiguration.class)
@RequiredArgsConstructor
public class LsdFeignAutoConfiguration {
    public static final PathToNameMapper ALWAYS_APP = path -> "App";

    private final TestState interactions;
    private final PathToNameMapper defaultSourceNameMapping;
    private final PathToNameMapper defaultDestinationNameMapping;

    @Bean
    public LsdFeignLoggerInterceptor lsdFeignLoggerInterceptor() {
        return new LsdFeignLoggerInterceptor(interactions, defaultSourceNameMapping, defaultDestinationNameMapping);
    }

    @Bean
    @ConditionalOnMissingBean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    static class NamingConfig {
        @Bean
        @ConditionalOnMissingBean(name = "defaultSourceNameMapping")
        public PathToNameMapper defaultSourceNameMapping() {
            return ALWAYS_APP;
        }

        @Bean
        @ConditionalOnMissingBean(name = "defaultDestinationNameMapping")
        public PathToNameMapper defaultDestinationNameMapping() {
            return new RegexResolvingNameMapper();
        }
    }

}
