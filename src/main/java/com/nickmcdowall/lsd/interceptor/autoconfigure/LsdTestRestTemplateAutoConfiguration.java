package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.DestinationNamesMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdRestTemplateInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static com.nickmcdowall.lsd.interceptor.common.RestTemplateModifier.addRestInterceptor;

/**
 * If a TestRestTemplate class and a TestState bean is available it will automatically autoconfig a RestTemplateLsdInterceptor
 */
@Configuration
@AutoConfigureAfter(value = {LsdDestinationNameMappingConfiguration.class})
@ConditionalOnBean(value = {TestState.class})
@ConditionalOnClass(value = TestRestTemplate.class)
@RequiredArgsConstructor
public class LsdTestRestTemplateAutoConfiguration {

    private final TestState interactions;
    private final TestRestTemplate testRestTemplate;
    private final DestinationNamesMapper testRestTemplateDestinationMappings;

    @PostConstruct
    public void configureInterceptor() {
        addRestInterceptor(testRestTemplate.getRestTemplate(), new LsdRestTemplateInterceptor(interactions, "User", testRestTemplateDestinationMappings));
    }

}
