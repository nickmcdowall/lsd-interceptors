package com.nickmcdowall.lsd.http.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.naming.ApplicationPaths;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
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

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LsdSourceAndDestinationNamesAutoConfigurationTest {

    private final LsdSourceAndDestinationNamesAutoConfiguration autoConfiguration = new LsdSourceAndDestinationNamesAutoConfiguration();
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LsdSourceAndDestinationNamesAutoConfiguration.class));

    @Mock
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    void sourceNameIsUserIfPathIsAnApplicationPath() {
        SourceNameMappings sourceNameMappings = autoConfiguration.defaultSourceNameMapping(new ApplicationPaths(of(
                "/product-details/meta-data/",
                "/error"
        )));

        assertThat(sourceNameMappings.mapForPath("/product-details/meta-data/")).isEqualTo("User");
        assertThat(sourceNameMappings.mapForPath("/error")).isEqualTo("User");
    }

    @Test
    void sourceNameIsAppIfPathIsNotAnApplicationPath() {
        SourceNameMappings sourceNameMappings = autoConfiguration.defaultSourceNameMapping(new ApplicationPaths(of(
        )));

        assertThat(sourceNameMappings.mapForPath("/random")).isEqualTo("App");
    }

    @Test
    void destinationNameIsAppIfPathIsAnApplicationPath() {
        DestinationNameMappings destinationNameMappings = autoConfiguration.defaultDestinationNameMapping(new ApplicationPaths(of(
                "/product-details/meta-data/",
                "/error"
        )));

        assertThat(destinationNameMappings.mapForPath("/product-details/meta-data/")).isEqualTo("App");
        assertThat(destinationNameMappings.mapForPath("/error")).isEqualTo("App");
    }

    @Test
    void destinationNameIsBasedOnPathIfPathIsNotAnApplicationPath() {
        DestinationNameMappings destinationNameMappings = autoConfiguration.defaultDestinationNameMapping(new ApplicationPaths(of()));

        assertThat(destinationNameMappings.mapForPath("/random")).isEqualTo("random");
    }

    @Test
    void extractsHandlerMappingPathPrefixesWithoutVariables() {
        when(handlerMapping.getHandlerMethods()).thenReturn(aMapWithKey(RequestMappingInfo.paths(
                "/product-details/meta-data/{key}",
                "/a/b/c/{d}/e"
        ).build()));

        ApplicationPaths applicationPaths = autoConfiguration.applicationPaths(handlerMapping);

        assertThat(applicationPaths.stream()).containsAll(of("/product-details/meta-data/", "/a/b/c/"));
    }

    @Test
    void includesCommonApplicationPathPrefixesByDefault() {
        when(handlerMapping.getHandlerMethods()).thenReturn(aMapWithKey(RequestMappingInfo.paths().build()));

        ApplicationPaths applicationPaths = autoConfiguration.applicationPaths(handlerMapping);

        assertThat(applicationPaths.stream()).containsAll(of(
                "/actuator",
                "/swagger-ui.html"
        ));
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

    @Test
    void noBeansAutoLoadedWhenDisabledViaProperty() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans.class)
                .withPropertyValues("yatspec.lsd.interceptors.autoconfig.enabled=false")
                .run((context) -> {
                    assertThat(context).doesNotHaveBean("defaultSourceNameMapping");
                    assertThat(context).doesNotHaveBean("defaultDestinationNameMapping");
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

    private Map<RequestMappingInfo, HandlerMethod> aMapWithKey(RequestMappingInfo mappingInfo) {
        return Map.of(mappingInfo, mock(HandlerMethod.class));
    }
}