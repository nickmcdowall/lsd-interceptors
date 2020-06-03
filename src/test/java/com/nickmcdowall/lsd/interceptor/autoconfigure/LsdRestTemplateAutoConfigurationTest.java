package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.RegexResolvingDestinationNameMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdRestTemplateInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static com.nickmcdowall.lsd.interceptor.autoconfigure.LsdNameMappingConfiguration.ALWAYS_APP;
import static org.assertj.core.api.Assertions.assertThat;

class LsdRestTemplateAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LsdRestTemplateAutoConfiguration.class,
                    LsdNameMappingConfiguration.class
            ));

    @Test
    public void noInterceptorAddedWhenNoTestStateBeanExists() {
        contextRunner.withUserConfiguration(UserConfigWithNoTestState.class).run((context) -> {
            assertThat(context).hasSingleBean(RestTemplate.class);
            assertThat(context.getBean(RestTemplate.class).getInterceptors()).isEmpty();
        });
    }

    @Test
    void interceptorAddedWhenTestStateExists() {
        contextRunner.withUserConfiguration(UserConfigWithTestState.class).run((context) -> {
            assertThat(context).hasSingleBean(RestTemplate.class);
            assertThat(context.getBean(RestTemplate.class).getInterceptors()).containsExactly(
                    new LsdRestTemplateInterceptor(new TestState(), ALWAYS_APP, new RegexResolvingDestinationNameMapper())
            );
        });
    }

    @Configuration
    static class UserConfigWithNoTestState {
        @Bean
        public RestTemplate myRestTemplate() {
            return new RestTemplate();
        }
    }

    @Configuration
    static class UserConfigWithTestState {
        @Bean
        public RestTemplate myRestTemplate() {
            return new RestTemplate();
        }

        @Bean
        public TestState interactions() {
            return new TestState();
        }
    }

}