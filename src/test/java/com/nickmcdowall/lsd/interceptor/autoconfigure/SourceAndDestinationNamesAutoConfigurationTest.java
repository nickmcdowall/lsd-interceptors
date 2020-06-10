package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;

class SourceAndDestinationNamesAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SourceAndDestinationNamesAutoConfiguration.class
            ));

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