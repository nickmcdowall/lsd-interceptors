package io.lsdconsulting.interceptors.http;

import com.lsd.LsdContext;
import io.lsdconsulting.interceptors.http.LsdRestTemplateCustomizer;
import io.lsdconsulting.interceptors.http.LsdRestTemplateInterceptor;
import io.lsdconsulting.interceptors.http.common.DefaultHttpInteractionHandler;
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler;
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
    private final LsdContext lsdContext = LsdContext.getInstance();
    private final List<HttpInteractionHandler> httpInteractionHandlers = List.of(new DefaultHttpInteractionHandler(lsdContext, s -> "Source", s -> "Destination"));
    private final LsdRestTemplateInterceptor lsdRestTemplateInterceptor = new LsdRestTemplateInterceptor(httpInteractionHandlers);
    private final LsdRestTemplateCustomizer lsdRestTemplateCustomizer = new LsdRestTemplateCustomizer(lsdRestTemplateInterceptor);


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