package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.DestinationNamesMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdOkHttpInterceptor;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * If a `OkHttpClient.Builder` bean is available we can add an interceptor to the builder before it gets used to build
 * a `OkHttpClient`. We can't modify the client instance once it has been built without creating a separate instance so
 * we rely on the builder been being available.
 */
@Configuration
@ConditionalOnBean(value = {TestState.class, OkHttpClient.Builder.class})
@AutoConfigureAfter(LsdDestinationNameMappingConfiguration.class)
@RequiredArgsConstructor
public class LsdOkHttpAutoConfiguration {

    private final TestState interactions;
    private final OkHttpClient.Builder okHttpClientBuilder;
    private final DestinationNamesMapper defaultAppToDestinationNameMappings;

    @PostConstruct
    public void configureInterceptor() {
        okHttpClientBuilder.addInterceptor(
                new LsdOkHttpInterceptor(interactions, "App", defaultAppToDestinationNameMappings)
        );
    }
}
