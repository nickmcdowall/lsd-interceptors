package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.PathToNameMapper;
import com.nickmcdowall.lsd.interceptor.common.RegexResolvingDestinationNameMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdRestTemplateInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static com.nickmcdowall.lsd.interceptor.autoconfigure.LsdRestTemplateAutoConfiguration.ALWAYS_APP;
import static com.nickmcdowall.lsd.interceptor.common.UserSuppliedMappings.userSuppliedMappings;
import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;

class LsdRestTemplateAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LsdRestTemplateAutoConfiguration.class
            ));

    @Test
    public void noBeansAutoLoadedWhenRequiredBeansMissing() {
        contextRunner.withUserConfiguration(UserConfigWithoutRequiredBeans.class).run((context) -> {
            assertThat(context).doesNotHaveBean("defaultRestTemplateSourceNameMapping");
            assertThat(context).doesNotHaveBean("defaultRestTemplateDestinationNameMapping");
            assertThat(context).doesNotHaveBean(RestTemplate.class);
        });
    }

    @Test
    public void noInterceptorAddedWhenNoTestStateBeanExists() {
        contextRunner.withUserConfiguration(UserConfigWithNoTestState.class).run((context) -> {
            assertThat(context).hasSingleBean(RestTemplate.class);
            assertThat(context.getBean(RestTemplate.class).getInterceptors()).isEmpty();
        });
    }

    @Test
    void restTemplateInterceptorAdded() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans.class).run((context) -> {
            assertThat(context).hasSingleBean(RestTemplate.class);
            assertThat(context).hasBean("defaultRestTemplateSourceNameMapping");
            assertThat(context).hasBean("defaultRestTemplateDestinationNameMapping");
            assertThat(context.getBean(RestTemplate.class).getInterceptors()).containsExactly(
                    new LsdRestTemplateInterceptor(new TestState(), ALWAYS_APP, new RegexResolvingDestinationNameMapper())
            );
        });
    }

    @Test
    void userCanOverrideNameMappings() {
        contextRunner.withUserConfiguration(UserConfigWithNameMappingOverrides.class).run((context) -> {
            assertThat(context).getBean("defaultRestTemplateSourceNameMapping", PathToNameMapper.class)
                    .isEqualTo(userSuppliedMappings(of("/source", "Source")));
            assertThat(context).getBean("defaultRestTemplateDestinationNameMapping", PathToNameMapper.class)
                    .isEqualTo(userSuppliedMappings(of("/destination", "Destination")));
        });
    }

    @Configuration
    static class UserConfigWithoutRequiredBeans {
    }

    @Configuration
    static class UserConfigWithNoTestState {
        @Bean
        public RestTemplate myRestTemplate() {
            return new RestTemplate();
        }
    }

    @Configuration
    static class UserConfigWithRequiredBeans {
        @Bean
        public RestTemplate myRestTemplate() {
            return new RestTemplate();
        }

        @Bean
        public TestState interactions() {
            return new TestState();
        }
    }

    @Configuration
    static class UserConfigWithNameMappingOverrides {
        @Bean
        public RestTemplate myRestTemplate() {
            return new RestTemplate();
        }

        @Bean
        public TestState interactions() {
            return new TestState();
        }

        @Bean
        public PathToNameMapper defaultRestTemplateSourceNameMapping() {
            return userSuppliedMappings(of("/source", "Source"));
        }

        @Bean
        public PathToNameMapper defaultRestTemplateDestinationNameMapping() {
            return userSuppliedMappings(of("/destination", "Destination"));
        }
    }

}