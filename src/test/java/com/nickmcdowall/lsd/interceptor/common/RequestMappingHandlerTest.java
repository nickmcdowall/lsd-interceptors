package com.nickmcdowall.lsd.interceptor.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RequestMappingHandlerTest {

    @Mock
    private RequestMappingHandlerMapping handlerMapping;

    @Mock
    private HandlerMethod handlerMethod;

    private RequestMappingExtractor converter = new RequestMappingExtractor();

    @Test
    void convertsToASetOfPaths() {
        RequestMappingInfo mappingInfo = RequestMappingInfo.paths(
                "/product-details/{id}", "/product-details/meta-data/{key}", "/error", "/a/b/c/{d}/e")
                .build();
        when(handlerMapping.getHandlerMethods()).thenReturn(Map.of(mappingInfo, handlerMethod));

        Set<String> pathSet = converter.extractPathPrefixes(handlerMapping);

        assertThat(pathSet).containsExactlyInAnyOrder("/product-details/", "/product-details/meta-data/", "/error", "/a/b/c/");
    }

}
