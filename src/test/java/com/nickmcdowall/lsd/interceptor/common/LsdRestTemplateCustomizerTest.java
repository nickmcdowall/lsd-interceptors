package com.nickmcdowall.lsd.interceptor.common;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.rest.LsdRestTemplateCustomizer;
import com.nickmcdowall.lsd.interceptor.rest.LsdRestTemplateInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LsdRestTemplateCustomizerTest {

    private final RestTemplate restTemplate = new RestTemplate();
    private final TestState interactions = new TestState();
    private final LsdRestTemplateInterceptor lsdRestTemplateInterceptor = new LsdRestTemplateInterceptor(interactions, s -> "Source", s -> "Destination");

    private LsdRestTemplateCustomizer lsdRestTemplateCustomizer = new LsdRestTemplateCustomizer(lsdRestTemplateInterceptor);


    @Test
    void addsLsdInterceptor() {
        lsdRestTemplateCustomizer.customize(restTemplate);

        assertThat(restTemplate.getInterceptors()).containsExactly(lsdRestTemplateInterceptor);
    }

    @Test
    void preservesExistingCustomizers() {
        restTemplate.setInterceptors(List.of(mock(ClientHttpRequestInterceptor.class)));

        lsdRestTemplateCustomizer.customize(restTemplate);

        assertThat(restTemplate.getInterceptors()).hasSize(2);
    }

    @Test
    void doesntAddDuplicateInterceptor() {
        restTemplate.setInterceptors(List.of(lsdRestTemplateInterceptor));

        lsdRestTemplateCustomizer.customize(restTemplate);

        assertThat(restTemplate.getInterceptors()).hasSize(1);
    }

    @Test
    void responseIsNotEmptyAfterInterception() {
        lsdRestTemplateCustomizer.customize(restTemplate);

        String forObject = restTemplate.getForObject("https://httpbin.org/get", String.class);

        assertThat(forObject).isNotEmpty();
    }
}