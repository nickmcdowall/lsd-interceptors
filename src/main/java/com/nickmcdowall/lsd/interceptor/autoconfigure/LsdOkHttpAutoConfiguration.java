package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.PathToNameMapper;
import com.nickmcdowall.lsd.interceptor.common.RegexResolvingDestinationNameMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdOkHttpInterceptor;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * <p>
 * If an {@link OkHttpClient.Builder} bean is available we can add an interceptor to the builder before it gets used to build
 * a `OkHttpClient`. We can't modify the client instance once it has been built without creating a separate instance so
 * we rely on the builder been being available.
 * </p>
 * <br/>
 * <p>
 * It is assumed that the {@link okhttp3.OkHttpClient} will be used to invoke downstream endpoints from within the app.
 * Therefore the <em>source</em> name will default to <em>'App'</em> and the <em>destination</em> name will be derived via
 * a {@link RegexResolvingDestinationNameMapper} by default.
 * </p>
 * <br/>
 * <p>
 * Users can override either or both of the default mappings by supplying their own {@link PathToNameMapper} beans and naming them
 * <em>'defaultOkHttpSourceNameMapping`</em> for source names and <em>'defaultOkHttpDestinationNameMapping`</em> for destination names.
 * </p>
 */
@Configuration
@ConditionalOnBean(value = {TestState.class, OkHttpClient.Builder.class})
@ConditionalOnProperty(value = "com.lsd.intercept.okhttp", havingValue = "true")
@RequiredArgsConstructor
public class LsdOkHttpAutoConfiguration {
    public static final PathToNameMapper ALWAYS_APP = path -> "App";

    private final TestState interactions;
    private final OkHttpClient.Builder okHttpClientBuilder;
    private final PathToNameMapper defaultOkHttpSourceNameMapping;
    private final PathToNameMapper defaultOkHttpDestinationNameMapping;

    @PostConstruct
    public void configureInterceptor() {
        okHttpClientBuilder.addInterceptor(
                new LsdOkHttpInterceptor(interactions, defaultOkHttpSourceNameMapping, defaultOkHttpDestinationNameMapping)
        );
    }

    @Configuration
    static class NameMapping {
        @Bean
        @ConditionalOnMissingBean(name = "defaultOkHttpSourceNameMapping")
        public PathToNameMapper defaultOkHttpSourceNameMapping() {
            return ALWAYS_APP;
        }

        @Bean
        @ConditionalOnMissingBean(name = "defaultOkHttpDestinationNameMapping")
        public PathToNameMapper defaultOkHttpDestinationNameMapping() {
            return new RegexResolvingDestinationNameMapper();
        }
    }
}
