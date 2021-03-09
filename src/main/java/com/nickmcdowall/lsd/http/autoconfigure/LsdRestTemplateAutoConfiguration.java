package com.nickmcdowall.lsd.http.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.common.HttpInteractionHandler;
import com.nickmcdowall.lsd.http.interceptor.LsdRestTemplateCustomizer;
import com.nickmcdowall.lsd.http.interceptor.LsdRestTemplateInterceptor;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.RegexResolvingNameMapper;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import java.util.List;


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
 * Users can override either or both of the default name mappings by supplying their own {@link SourceNameMappings} or
 * {@link DestinationNameMappings} beans and naming them <em>'defaultSourceNameMapping`</em> and <em>'defaultDestinationNameMapping`</em>.
 * </p>
 */
@Configuration
@ConditionalOnProperty(name = "yatspec.lsd.interceptors.autoconfig.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(value = {TestState.class})
@ConditionalOnClass(value = {RestTemplate.class})
@Import({NamingConfig.class, HttpHandlerConfig.class})
@RequiredArgsConstructor
public class LsdRestTemplateAutoConfiguration {

    private final List<HttpInteractionHandler> httpInteractionHandlers;

    @Bean
    public RestTemplateCustomizer restTemplateCustomizer() {
        return new LsdRestTemplateCustomizer(new LsdRestTemplateInterceptor(httpInteractionHandlers));
    }
}
