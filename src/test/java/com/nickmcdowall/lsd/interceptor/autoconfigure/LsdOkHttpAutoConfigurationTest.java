package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.PathToNameMapper;
import com.nickmcdowall.lsd.interceptor.common.RegexResolvingNameMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdOkHttpInterceptor;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.nickmcdowall.lsd.interceptor.autoconfigure.LsdOkHttpAutoConfiguration.ALWAYS_APP;
import static com.nickmcdowall.lsd.interceptor.common.UserSuppliedMappings.userSuppliedMappings;
import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;

public class LsdOkHttpAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LsdOkHttpAutoConfiguration.class
            ));

    @Test
    public void noBeansAutoLoadedWhenRequiredBeansMissing() {
        contextRunner.withUserConfiguration(UserConfigWithoutRequiredBeans.class)
                .withPropertyValues("com.lsd.intercept.okhttp=true")
                .run((context) -> {
                    assertThat(context).doesNotHaveBean("defaultOkHttpSourceNameMapping");
                    assertThat(context).doesNotHaveBean("defaultOkHttpDestinationNameMapping");
                    assertThat(context).doesNotHaveBean(OkHttpClient.Builder.class);
                });
    }

    @Test
    public void addsOkHttpBuilderWhenPropertySetAndRequiredBeansAvailable() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans.class)
                .withPropertyValues("com.lsd.intercept.okhttp=true")
                .run((context) -> {
                    assertThat(context).hasBean("defaultOkHttpSourceNameMapping");
                    assertThat(context).hasBean("defaultOkHttpDestinationNameMapping");
                    assertThat(context).hasSingleBean(OkHttpClient.Builder.class);
                    assertThat(context.getBean(OkHttpClient.Builder.class).interceptors()).containsExactly(
                            new LsdOkHttpInterceptor(new TestState(), ALWAYS_APP, new RegexResolvingNameMapper()));
                });
    }

    @Test
    public void noInterceptorWhenPropertyNotSet() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans.class).run((context) -> {
            assertThat(context).doesNotHaveBean("defaultOkHttpSourceNameMapping");
            assertThat(context).doesNotHaveBean("defaultOkHttpDestinationNameMapping");
            assertThat(context.getBean(OkHttpClient.Builder.class).interceptors()).isEmpty();
        });
    }

    @Test
    void userCanOverrideNameMappings() {
        contextRunner.withUserConfiguration(UserConfigWithNameMappingOverrides.class)
                .withPropertyValues("com.lsd.intercept.okhttp=true")
                .run((context) -> {
                    assertThat(context).getBean("defaultOkHttpSourceNameMapping", PathToNameMapper.class)
                            .isEqualTo(userSuppliedMappings(of("/source", "Source")));
                    assertThat(context).getBean("defaultOkHttpDestinationNameMapping", PathToNameMapper.class)
                            .isEqualTo(userSuppliedMappings(of("/destination", "Destination")));
                });
    }

    @Configuration
    static class UserConfigWithoutRequiredBeans {
    }

    @Configuration
    static class UserConfigWithRequiredBeans {
        @Bean
        public TestState testState() {
            return new TestState();
        }

        @Bean
        public OkHttpClient.Builder httpClient() {
            return new OkHttpClient.Builder();
        }
    }

    @Configuration
    static class UserConfigWithNameMappingOverrides {

        @Bean
        @ConditionalOnMissingBean(name = "defaultOkHttpSourceNameMapping")
        public PathToNameMapper defaultOkHttpSourceNameMapping() {
            return userSuppliedMappings(of("/source", "Source"));
        }

        @Bean
        public PathToNameMapper defaultOkHttpDestinationNameMapping() {
            return userSuppliedMappings(of("/destination", "Destination"));
        }

        @Bean
        public TestState testState() {
            return new TestState();
        }

        @Bean
        public OkHttpClient.Builder httpClient() {
            return new OkHttpClient.Builder();
        }
    }
}
