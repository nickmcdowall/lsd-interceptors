package com.nickmcdowall.lsd.http.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.common.DefaultHttpInteractionHandler;
import com.nickmcdowall.lsd.http.common.HttpInteractionHandler;
import com.nickmcdowall.lsd.http.interceptor.LsdOkHttpInterceptor;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.RegexResolvingNameMapper;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * <p>
 * If an {@link OkHttpClient.Builder} bean is available we can add an interceptor to the builder before it gets used to build
 * a `OkHttpClient`. We can't modify the client instance once it has been built without creating a separate instance so
 * we rely on the builder been being available.
 * </p>
 * <br>
 * <p>
 * It is assumed that the {@link okhttp3.OkHttpClient} will be used to invoke downstream endpoints from within the app.
 * Therefore the <em>source</em> name will default to <em>'App'</em> and the <em>destination</em> name will be derived via
 * a {@link RegexResolvingNameMapper} by default.
 * </p>
 * <br>
 * <p>
 * Users can override either or both of the default name mappings by supplying their own {@link SourceNameMappings} or
 * {@link DestinationNameMappings} beans and naming them <em>'defaultSourceNameMapping`</em> and <em>'defaultDestinationNameMapping`</em>.
 * </p>
 */
@Configuration
@ConditionalOnBean(value = {TestState.class, OkHttpClient.Builder.class})
@ConditionalOnProperty(value = "yatspec.lsd.interceptors.autoconfig.okhttp.enabled", havingValue = "true")
@Import({NamingConfig.class, HttpHandlerConfig.class})
@RequiredArgsConstructor
public class LsdOkHttpAutoConfiguration {

    private final List<HttpInteractionHandler> httpInteractionHandlers;
    private final OkHttpClient.Builder okHttpClientBuilder;

    @PostConstruct
    public void configureInterceptor() {
        okHttpClientBuilder.addInterceptor(new LsdOkHttpInterceptor(httpInteractionHandlers));
    }
}
