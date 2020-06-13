package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.PathToNameMapper;
import com.nickmcdowall.lsd.interceptor.common.RegexResolvingNameMapper;
import com.nickmcdowall.lsd.interceptor.common.LsdRestTemplateCustomizer;
import com.nickmcdowall.lsd.interceptor.rest.LsdRestTemplateInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


/**
 * <p>
 * If a {@link RestTemplate} class and a {@link TestState} bean is available it will automatically autoconfig
 * a {@link LsdRestTemplateInterceptor}
 * </p>
 * <br/>
 * <p>
 * It is assumed that if a {@link RestTemplate} bean exists is will be used to invoke downstream endpoints from within the app.
 * Therefore the <em>source</em> name will default to <em>'App'</em> and the <em>destination</em> name will be derived using a
 * {@link RegexResolvingNameMapper} by default.
 * </p>
 * <br/>
 * <p>
 * Users can override the default name mappings by supplying their own {@link PathToNameMapper} beans and calling it
 * <em>'defaultRestTemplateSourceNameMapping`</em> for source names and <em>'defaultRestTemplateDestinationNameMapping`</em>
 * for destination names.
 * </p>
 */
@Configuration
@ConditionalOnBean(value = {TestState.class})
@ConditionalOnClass(value = {RestTemplate.class})
@AutoConfigureAfter(LsdSourceAndDestinationNamesAutoConfiguration.class)
@RequiredArgsConstructor
public class LsdRestTemplateAutoConfiguration {
    public static final PathToNameMapper ALWAYS_APP = path -> "App";

    private final TestState interactions;
    private final PathToNameMapper defaultSourceNameMapping;
    private final PathToNameMapper defaultDestinationNameMapping;

    @Bean
    public RestTemplateCustomizer restTemplateCustomizer() {
        return new LsdRestTemplateCustomizer(
                new LsdRestTemplateInterceptor(interactions, defaultSourceNameMapping, defaultDestinationNameMapping));
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
