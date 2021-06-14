package com.nickmcdowall.lsd.http.autoconfigure;

import com.lsd.LsdContext;
import com.nickmcdowall.lsd.http.common.HttpInteractionHandler;
import com.nickmcdowall.lsd.http.interceptor.LsdFeignLoggerInterceptor;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.RegexResolvingNameMapper;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import feign.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

/**
 * <p>
 * If a {@link com.lsd.LsdContext} class is available it will automatically autoconfig a {@link LsdFeignLoggerInterceptor}
 * </p>
 * <br>
 * <p>
 * By default <em>source</em> name defaults to <em>'App'</em> and the <em>destination</em> name will be derived using a
 * {@link RegexResolvingNameMapper}.
 * </p>
 * <br>
 * <p>
 * Users can override either or both of the default name mappings by supplying their own {@link SourceNameMappings} or
 * {@link DestinationNameMappings} beans and naming them <em>'defaultSourceNameMapping`</em> and <em>'defaultDestinationNameMapping`</em>.
 * </p>
 */
@Configuration
@PropertySource(ignoreResourceNotFound = true, value = "classpath:lsd.properties")
@ConditionalOnProperty(name = "lsd.interceptors.autoconfig.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(value = {LsdContext.class, FeignClientBuilder.class, Logger.Level.class})
@Import({NamingConfig.class, HttpHandlerConfig.class})
@RequiredArgsConstructor
public class LsdFeignAutoConfiguration {

    private final List<HttpInteractionHandler> httpInteractionHandlers;

    @Bean
    public LsdFeignLoggerInterceptor lsdFeignLoggerInterceptor() {
        return new LsdFeignLoggerInterceptor(httpInteractionHandlers);
    }

    @Bean
    @ConditionalOnMissingBean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
