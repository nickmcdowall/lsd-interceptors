package com.nickmcdowall.lsd.http.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.interceptor.LsdRestTemplateCustomizer;
import com.nickmcdowall.lsd.http.interceptor.LsdRestTemplateInterceptor;
import com.nickmcdowall.lsd.http.naming.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;

class LsdRestTemplateAutoConfigurationTest {
    private static final SourceNameMappings SOURCE_NAMES_OVERRIDE = UserSuppliedSourceMappings.userSuppliedSourceMappings(of("/source", "Source"));
    private static final DestinationNameMappings DESTINATION_NAMES_OVERRIDE = UserSuppliedDestinationMappings.userSuppliedDestinationMappings(of("/destination", "Destination"));

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LsdRestTemplateAutoConfiguration.class
            ));

    @Test
    public void noBeansAutoLoadedWhenRequiredBeansMissing() {
        contextRunner.withUserConfiguration(UserConfigWithoutRequiredBeans.class).run((context) -> {
            assertThat(context).doesNotHaveBean("defaultSourceNameMapping");
            assertThat(context).doesNotHaveBean("defaultDestinationNameMapping");
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
            assertThat(context).hasBean("defaultSourceNameMapping");
            assertThat(context).hasBean("defaultDestinationNameMapping");
            assertThat(context).getBean(LsdRestTemplateCustomizer.class).isEqualTo(
                    new LsdRestTemplateCustomizer(new LsdRestTemplateInterceptor(new TestState(), SourceNameMappings.ALWAYS_APP, new RegexResolvingNameMapper())));
        });
    }

    @Test
    void userCanOverrideNameMappings() {
        contextRunner.withUserConfiguration(UserConfigWithNameMappingOverrides.class).run((context) -> {
            assertThat(context).getBean("defaultSourceNameMapping", SourceNameMappings.class)
                    .isEqualTo(SOURCE_NAMES_OVERRIDE);
            assertThat(context).getBean("defaultDestinationNameMapping", DestinationNameMappings.class)
                    .isEqualTo(DESTINATION_NAMES_OVERRIDE);
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
        public SourceNameMappings defaultSourceNameMapping() {
            return SOURCE_NAMES_OVERRIDE;
        }

        @Bean
        public DestinationNameMappings defaultDestinationNameMapping() {
            return DESTINATION_NAMES_OVERRIDE;
        }
    }

}