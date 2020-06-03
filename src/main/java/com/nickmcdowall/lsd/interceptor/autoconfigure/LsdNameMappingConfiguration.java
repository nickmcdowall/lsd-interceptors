package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.nickmcdowall.lsd.interceptor.common.PathToNameMapper;
import com.nickmcdowall.lsd.interceptor.common.RegexResolvingDestinationNameMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Autoconfigures the default {@link PathToNameMapper} beans to be used by the various http interceptors.
 */
@Configuration
public class LsdNameMappingConfiguration {

    public static final PathToNameMapper ALWAYS_APP = path -> "App";
    public static final PathToNameMapper ALWAYS_USER = path -> "User";

    /**
     * <p>
     * It is assumed that if a {@link TestRestTemplate} bean exists is will be used to invoke the application endpoint.
     * Therefore the <em>source</em> name will map to <em>'User'</em> by default.
     * </p>
     * <br/>
     * <p>
     * Users can override the default mapping by supplying their own {@link PathToNameMapper} bean and calling it
     * <em>'defaultTestRestTemplateSourceNameMapping`</em>.
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "defaultTestRestTemplateSourceNameMapping")
    public PathToNameMapper defaultTestRestTemplateSourceNameMapping() {
        return ALWAYS_USER;
    }

    /**
     * <p>
     * It is assumed that if a {@link TestRestTemplate} bean exists is will be used to invoke the application endpoint.
     * Therefore the <em>destination</em> name will map to <em>'App'</em> by default.
     * </p>
     * <br/>
     * <p>
     * Users can override the default mapping by supplying their own {@link PathToNameMapper} bean and calling it
     * <em>'defaultTestRestTemplateDestinationNameMapping`</em>.
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "defaultTestRestTemplateDestinationNameMapping")
    public PathToNameMapper defaultTestRestTemplateDestinationNameMapping() {
        return ALWAYS_APP;
    }

    /**
     * <p>
     * It is assumed that if a {@link RestTemplate} bean exists is will be used to invoke downstream endpoints from within the app.
     * Therefore the <em>source</em> name will map to <em>'App'</em> by default.
     * </p>
     * <br/>
     * <p>
     * Users can override the default mapping by supplying their own {@link PathToNameMapper} bean and calling it
     * <em>'defaultRestTemplateSourceNameMapping`</em>.
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "defaultRestTemplateSourceNameMapping")
    public PathToNameMapper defaultRestTemplateSourceNameMapping() {
        return ALWAYS_APP;
    }

    /**
     * <p>
     * It is assumed that if a {@link RestTemplate} bean exists is will be used to invoke downstream endpoints from within the app.
     * Therefore the <em>destination</em> name will map using a {@link RegexResolvingDestinationNameMapper} by default.
     * </p>
     * <br/>
     * <p>
     * Users can override the default mapping by supplying their own {@link PathToNameMapper} bean and calling it
     * <em>'defaultRestTemplateDestinationNameMapping`</em>.
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "defaultRestTemplateDestinationNameMapping")
    public PathToNameMapper defaultRestTemplateDestinationNameMapping() {
        return new RegexResolvingDestinationNameMapper();
    }


    /**
     * <p>
     * It is assumed that if a {@link okhttp3.OkHttpClient} bean exists is will be used to invoke downstream endpoints from within the app.
     * Therefore the <em>source</em> name will map using <em>'App'</em> by default.
     * </p>
     * <br/>
     * <p>
     * Users can override the default mapping by supplying their own {@link PathToNameMapper} bean and calling it
     * <em>'defaultOkHttpSourceNameMapping`</em>.
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "defaultOkHttpSourceNameMapping")
    public PathToNameMapper defaultOkHttpSourceNameMapping() {
        return ALWAYS_APP;
    }

    /**
     * <p>
     * It is assumed that if a {@link okhttp3.OkHttpClient} bean exists is will be used to invoke downstream endpoints from within the app.
     * Therefore the <em>destination</em> name will map using a {@link RegexResolvingDestinationNameMapper} by default.
     * </p>
     * <br/>
     * <p>
     * Users can override the default mapping by supplying their own {@link PathToNameMapper} bean and calling it
     * <em>'defaultOkHttpDestinationNameMapping`</em>.
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "defaultOkHttpDestinationNameMapping")
    public PathToNameMapper defaultOkHttpDestinationNameMapping() {
        return new RegexResolvingDestinationNameMapper();
    }

}
