package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.interceptor.naming.SourceNameMappings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LsdSourceAndDestinationNamesAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LsdSourceAndDestinationNamesAutoConfiguration.class));

    @Mock
    private RequestMappingHandlerMapping handlerMapping;

    @Mock
    private HandlerMethod handlerMethod;

    private LsdSourceAndDestinationNamesAutoConfiguration autoConfiguration = new LsdSourceAndDestinationNamesAutoConfiguration();

    @Test
    void extractsHanlderMappingPathsForDestinationNames() {
        RequestMappingInfo mappingInfo = RequestMappingInfo.paths(
                "/product-details/{id}",
                "/product-details/meta-data/{key}",
                "/error",
                "/a/b/c/{d}/e")
                .build();
        when(handlerMapping.getHandlerMethods()).thenReturn(Map.of(mappingInfo, handlerMethod));

        DestinationNameMappings destinationNameMappings = autoConfiguration.defaultDestinationNameMapping(handlerMapping);

        assertThat(destinationNameMappings.mapForPath("/product-details/")).isEqualTo("App");
        assertThat(destinationNameMappings.mapForPath("/product-details/meta-data/")).isEqualTo("App");
        assertThat(destinationNameMappings.mapForPath("/error")).isEqualTo("App");
        assertThat(destinationNameMappings.mapForPath("/a/b/c/")).isEqualTo("App");
        assertThat(destinationNameMappings.mapForPath("/random")).isEqualTo("random");
    }

    @Test
    void extractsHanlderMappingPathsForSourceNames() {
        RequestMappingInfo mappingInfo = RequestMappingInfo.paths(
                "/product-details/{id}",
                "/product-details/meta-data/{key}",
                "/error",
                "/a/b/c/{d}/e")
                .build();
        when(handlerMapping.getHandlerMethods()).thenReturn(Map.of(mappingInfo, handlerMethod));

        SourceNameMappings destinationNameMappings = autoConfiguration.defaultSourceNameMapping(handlerMapping);

        assertThat(destinationNameMappings.mapForPath("/product-details/")).isEqualTo("User");
        assertThat(destinationNameMappings.mapForPath("/product-details/meta-data/")).isEqualTo("User");
        assertThat(destinationNameMappings.mapForPath("/error")).isEqualTo("User");
        assertThat(destinationNameMappings.mapForPath("/a/b/c/")).isEqualTo("User");
        assertThat(destinationNameMappings.mapForPath("/random")).isEqualTo("App");
    }

    @Test
    public void noBeansAutoLoadedWhenRequiredBeansMissing() {
        contextRunner.withUserConfiguration(UserConfigWithoutRequiredBeans.class).run((context) -> {
            assertThat(context).doesNotHaveBean("defaultSourceNameMapping");
            assertThat(context).doesNotHaveBean("defaultDestinationNameMapping");
        });
    }

    @Test
    void loadsBeansWhenTestStateIsAvailable() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans.class).run((context) -> {
            assertThat(context).hasBean("defaultSourceNameMapping");
            assertThat(context).hasBean("defaultDestinationNameMapping");
        });
    }

    @Configuration
    static class UserConfigWithoutRequiredBeans {
    }

    @Configuration
    static class UserConfigWithRequiredBeans {
        @Bean
        public TestState interactions() {
            return new TestState();
        }

        @Bean
        public RequestMappingHandlerMapping requestMappingHandlerMapping() {
            return new RequestMappingHandlerMapping();
        }
    }
}