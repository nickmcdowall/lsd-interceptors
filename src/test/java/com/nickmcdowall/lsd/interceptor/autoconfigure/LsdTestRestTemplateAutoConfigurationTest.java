package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.RegexResolvingDestinationNameMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdRestTemplateInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class LsdTestRestTemplateAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LsdTestRestTemplateAutoConfiguration.class,
                    LsdDestinationNameMappingConfiguration.class
            ));

    @Test
    public void noInterceptorAddedWhenNoTestStateBeanExists() {
        contextRunner.withUserConfiguration(UserConfigWithNoTestState.class).run((context) -> {
            assertThat(context).hasSingleBean(TestRestTemplate.class);
            assertThat(context.getBean(TestRestTemplate.class).getRestTemplate().getInterceptors()).isEmpty();
        });
    }

    @Test
    void testRestTemplateInterceptorAdded() {
        contextRunner.withUserConfiguration(UserConfigWithTestRestTemplate.class).run((context) -> {
            assertThat(context).hasSingleBean(TestRestTemplate.class);
            assertThat(context.getBean(TestRestTemplate.class).getRestTemplate().getInterceptors()).containsExactly(
                    new LsdRestTemplateInterceptor(new TestState(), "User", new RegexResolvingDestinationNameMapper())
            );
        });
    }

    @Configuration
    static class UserConfigWithNoTestState {
        @Bean
        public TestRestTemplate myTestRestTemplate() {
            return new TestRestTemplate();
        }
    }

    @Configuration
    static class UserConfigWithTestRestTemplate {
        @Bean
        public TestRestTemplate testRestTemplate() {
            return new TestRestTemplate();
        }

        @Bean
        public TestState interactions() {
            return new TestState();
        }
    }

}