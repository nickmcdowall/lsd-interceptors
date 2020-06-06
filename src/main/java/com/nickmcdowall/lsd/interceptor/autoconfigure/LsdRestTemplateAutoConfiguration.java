package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.PathToNameMapper;
import com.nickmcdowall.lsd.interceptor.common.RegexResolvingDestinationNameMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdRestTemplateInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import static com.nickmcdowall.lsd.interceptor.common.RestTemplateModifier.addRestInterceptor;

/**
 * <p>
 * If a {@link RestTemplate} and a {@link TestState} bean is available it will automatically autoconfig
 * a {@link LsdRestTemplateInterceptor}
 *</p>
 * <br/>
 * <p>
 * It is assumed that if a {@link RestTemplate} bean exists is will be used to invoke downstream endpoints from within the app.
 * Therefore the <em>source</em> name will default to <em>'App'</em> and the <em>destination</em> name will be derived using a
 * {@link RegexResolvingDestinationNameMapper} by default.
 * </p>
 * <br/>
 * <p>
 * Users can override the default name mappings by supplying their own {@link PathToNameMapper} beans and calling it
 * <em>'defaultRestTemplateSourceNameMapping`</em> for source names and <em>'defaultRestTemplateDestinationNameMapping`</em>
 * for destination names.
 * </p>
 */
@Configuration
@ConditionalOnBean(value = {TestState.class, RestTemplate.class})
@RequiredArgsConstructor
public class LsdRestTemplateAutoConfiguration {
    public static final PathToNameMapper ALWAYS_APP = path -> "App";

    private final TestState interactions;
    private final RestTemplate restTemplate;
    private final PathToNameMapper defaultRestTemplateSourceNameMapping;
    private final PathToNameMapper defaultRestTemplateDestinationNameMapping;

    @PostConstruct
    public void configureInterceptor() {
        addRestInterceptor(restTemplate,
                new LsdRestTemplateInterceptor(interactions, defaultRestTemplateSourceNameMapping, defaultRestTemplateDestinationNameMapping));
    }

    static class NamingConfig {
        @Bean
        @ConditionalOnMissingBean(name = "defaultRestTemplateSourceNameMapping")
        public PathToNameMapper defaultRestTemplateSourceNameMapping() {
            return ALWAYS_APP;
        }

        @Bean
        @ConditionalOnMissingBean(name = "defaultRestTemplateDestinationNameMapping")
        public PathToNameMapper defaultRestTemplateDestinationNameMapping() {
            return new RegexResolvingDestinationNameMapper();
        }
    }

}
