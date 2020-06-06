package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.PathToNameMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdRestTemplateInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

import static com.nickmcdowall.lsd.interceptor.autoconfigure.LsdTestRestTemplateAutoConfiguration.NamingConfig;
import static com.nickmcdowall.lsd.interceptor.common.RestTemplateModifier.addRestInterceptor;

/**
 * <p>
 * If a {@link TestRestTemplate} class and a {@link TestState} bean is available it will automatically autoconfig
 * a {@link LsdRestTemplateInterceptor}
 * </p>
 * <br/>
 * <p>
 * It is assumed that a {@link TestRestTemplate} will be used to invoke the application endpoint.
 * Therefore the <em>source</em> name will be <em>'User'</em> by default and the <em>destination</em> name will
 * be <em>'App'</em> by default.
 * </p>
 * <br/>
 * <p>
 * Users can override the default mappings by supplying their own {@link PathToNameMapper} beans called
 * <em>'defaultTestRestTemplateSourceNameMapping`</em> for source name mappings and
 * <em>'defaultTestRestTemplateDestinationNameMapping`</em> for destination name mappings.
 * </p>
 */
@Configuration
@ConditionalOnBean(value = {TestState.class})
@ConditionalOnClass(value = TestRestTemplate.class)
@RequiredArgsConstructor
public class LsdTestRestTemplateAutoConfiguration {
    public static final PathToNameMapper ALWAYS_APP = path -> "App";
    public static final PathToNameMapper ALWAYS_USER = path -> "User";

    private final TestState interactions;
    private final TestRestTemplate testRestTemplate;
    private final PathToNameMapper defaultTestRestTemplateSourceNameMapping;
    private final PathToNameMapper defaultTestRestTemplateDestinationNameMapping;

    @PostConstruct
    public void configureInterceptor() {
        addRestInterceptor(testRestTemplate.getRestTemplate(),
                new LsdRestTemplateInterceptor(interactions, defaultTestRestTemplateSourceNameMapping, defaultTestRestTemplateDestinationNameMapping));
    }

    static class NamingConfig {
        @Bean
        @ConditionalOnMissingBean(name = "defaultTestRestTemplateSourceNameMapping")
        public PathToNameMapper defaultTestRestTemplateSourceNameMapping() {
            return ALWAYS_USER;
        }

        @Bean
        @ConditionalOnMissingBean(name = "defaultTestRestTemplateDestinationNameMapping")
        public PathToNameMapper defaultTestRestTemplateDestinationNameMapping() {
            return ALWAYS_APP;
        }
    }

}
