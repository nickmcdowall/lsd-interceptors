package com.nickmcdowall.lsd.interceptor.common;

import com.nickmcdowall.lsd.interceptor.rest.LsdRestTemplateInterceptor;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RestTemplateModifier {

    public static void addRestInterceptor(RestTemplate restTemplate, LsdRestTemplateInterceptor interceptor) {
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        BufferingClientHttpRequestFactory bufferedRequestFactory = new BufferingClientHttpRequestFactory(restTemplate.getRequestFactory());
        restTemplate.setRequestFactory(bufferedRequestFactory);
        restTemplate.getInterceptors().add(interceptor);
    }
}
