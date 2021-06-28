package io.lsdconsulting.interceptors.http.autoconfigure;

import com.lsd.LsdContext;
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler;
import io.lsdconsulting.interceptors.http.LsdRestTemplateCustomizer;
import io.lsdconsulting.interceptors.http.LsdRestTemplateInterceptor;
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings;
import io.lsdconsulting.interceptors.http.naming.RegexResolvingNameMapper;
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings;
import lombok.RequiredArgsConstructor;
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
 * If a {@link RestTemplate} class and a {@link com.lsd.LsdContext} class is available it will automatically autoconfig
 * a {@link LsdRestTemplateInterceptor}
 * </p>
 * <br>
 * <p>
 * It is assumed that if a {@link RestTemplate} bean exists is will be used to invoke downstream endpoints from within the app.
 * Therefore the <em>source</em> name will default to <em>'App'</em> and the <em>destination</em> name will be derived using a
 * {@link RegexResolvingNameMapper} by default.
 * </p>
 * <br>
 * <p>
 * Users can override either or both of the default name mappings by supplying their own {@link SourceNameMappings} or
 * {@link DestinationNameMappings} beans and naming them <em>'defaultSourceNameMapping`</em> and <em>'defaultDestinationNameMapping`</em>.
 * </p>
 */
@Configuration
@ConditionalOnProperty(name = "lsd.interceptors.autoconfig.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(value = {RestTemplate.class, LsdContext.class})
@Import({NamingConfig.class, HttpHandlerConfig.class})
@RequiredArgsConstructor
public class LsdRestTemplateAutoConfiguration {

    private final List<HttpInteractionHandler> httpInteractionHandlers;

    @Bean
    public RestTemplateCustomizer restTemplateCustomizer() {
        return new LsdRestTemplateCustomizer(new LsdRestTemplateInterceptor(httpInteractionHandlers));
    }
}
