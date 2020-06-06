package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.PathToNameMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdRestTemplateInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.nickmcdowall.lsd.interceptor.autoconfigure.LsdTestRestTemplateAutoConfiguration.ALWAYS_APP;
import static com.nickmcdowall.lsd.interceptor.autoconfigure.LsdTestRestTemplateAutoConfiguration.ALWAYS_USER;
import static com.nickmcdowall.lsd.interceptor.common.UserSuppliedMappings.userSuppliedMappings;
import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;

class LsdTestRestTemplateAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LsdTestRestTemplateAutoConfiguration.class
            ));

    @Test
    public void noBeansAutoLoaded() {
        contextRunner.withUserConfiguration(UserConfigWithoutRequiredBeans.class).run((context) -> {
            assertThat(context).doesNotHaveBean("defaultTestRestTemplateSourceNameMapping");
            assertThat(context).doesNotHaveBean("defaultTestRestTemplateDestinationNameMapping");
            assertThat(context).doesNotHaveBean(TestRestTemplate.class);
        });
    }

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
            assertThat(context).hasBean("defaultTestRestTemplateSourceNameMapping");
            assertThat(context).hasBean("defaultTestRestTemplateDestinationNameMapping");
            assertThat(context.getBean(TestRestTemplate.class).getRestTemplate().getInterceptors()).containsExactly(
                    new LsdRestTemplateInterceptor(new TestState(), ALWAYS_USER, ALWAYS_APP)
            );
        });
    }

    @Test
    void userCanOverrideNameMappings() {
        contextRunner.withUserConfiguration(UserConfigWithNameMappingOverrides.class).run((context) -> {
            assertThat(context).getBean("defaultTestRestTemplateSourceNameMapping", PathToNameMapper.class)
                    .isEqualTo(userSuppliedMappings(of("/source", "Source")));
            assertThat(context).getBean("defaultTestRestTemplateDestinationNameMapping", PathToNameMapper.class)
                    .isEqualTo(userSuppliedMappings(of("/destination", "Destination")));
        });
    }

    @Configuration
    static class UserConfigWithoutRequiredBeans {
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

    @Configuration
    static class UserConfigWithNameMappingOverrides {
        @Bean
        public TestRestTemplate testRestTemplate() {
            return new TestRestTemplate();
        }

        @Bean
        public TestState interactions() {
            return new TestState();
        }

        @Bean
        public PathToNameMapper defaultTestRestTemplateSourceNameMapping() {
            return userSuppliedMappings(of("/source", "Source"));
        }

        @Bean
        public PathToNameMapper defaultTestRestTemplateDestinationNameMapping() {
            return userSuppliedMappings(of("/destination", "Destination"));
        }

    }

}