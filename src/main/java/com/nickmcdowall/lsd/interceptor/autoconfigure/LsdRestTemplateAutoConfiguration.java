package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.DestinationNamesMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdRestTemplateInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import static com.nickmcdowall.lsd.interceptor.common.RestTemplateModifier.addRestInterceptor;

/**
 * If a RestTemplate and a TestState bean is available it will automatically autoconfig a RestTemplateLsdInterceptor
 */
@Configuration
@ConditionalOnBean(value = {TestState.class, RestTemplate.class})
@AutoConfigureAfter(LsdDestinationNameMappingConfiguration.class)
@RequiredArgsConstructor
public class LsdRestTemplateAutoConfiguration {

    private final TestState interactions;
    private final RestTemplate restTemplate;
    private final DestinationNamesMapper restTemplateDestinationMappings;

    @PostConstruct
    public void configureInterceptor() {
        addRestInterceptor(restTemplate, new LsdRestTemplateInterceptor(interactions, "App", restTemplateDestinationMappings));
    }

}
