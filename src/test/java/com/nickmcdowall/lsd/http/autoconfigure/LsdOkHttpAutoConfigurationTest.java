package com.nickmcdowall.lsd.http.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.common.DefaultHttpInteractionHandler;
import com.nickmcdowall.lsd.http.interceptor.LsdOkHttpInterceptor;
import com.nickmcdowall.lsd.http.naming.*;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
                .withPropertyValues("yatspec.lsd.interceptors.autoconfig.okhttp.enabled=true")
                .run((context) -> {
                    assertThat(context).doesNotHaveBean("defaultSourceNameMapping");
                    assertThat(context).doesNotHaveBean("defaultDestinationNameMapping");
                    assertThat(context).doesNotHaveBean(OkHttpClient.Builder.class);
                });
    }

    @Test
    public void addsOkHttpBuilderWhenPropertySetAndRequiredBeansAvailable() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans.class)
                .withPropertyValues("yatspec.lsd.interceptors.autoconfig.okhttp.enabled=true")
                .run((context) -> {
                    assertThat(context).hasBean("defaultSourceNameMapping");
                    assertThat(context).hasBean("defaultDestinationNameMapping");
                    assertThat(context).hasSingleBean(OkHttpClient.Builder.class);
                    assertThat(context.getBean(OkHttpClient.Builder.class).interceptors()).containsExactly(
                            new LsdOkHttpInterceptor(List.of(new DefaultHttpInteractionHandler(new TestState(), SourceNameMappings.ALWAYS_APP, new RegexResolvingNameMapper()))));
                });
    }

    @Test
    public void noInterceptorWhenPropertyNotSet() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans.class).run((context) -> {
            assertThat(context).doesNotHaveBean("defaultSourceNameMapping");
            assertThat(context).doesNotHaveBean("defaultDestinationNameMapping");
            assertThat(context.getBean(OkHttpClient.Builder.class).interceptors()).isEmpty();
        });
    }

    @Test
    void userCanOverrideNameMappings() {
        contextRunner.withUserConfiguration(UserConfigWithNameMappingOverrides.class)
                .withPropertyValues("yatspec.lsd.interceptors.autoconfig.okhttp.enabled=true")
                .run((context) -> {
                    assertThat(context).getBean("defaultSourceNameMapping", SourceNameMappings.class)
                            .isEqualTo(UserSuppliedSourceMappings.userSuppliedSourceMappings(of("/source", "Source")));
                    assertThat(context).getBean("defaultDestinationNameMapping", DestinationNameMappings.class)
                            .isEqualTo(UserSuppliedDestinationMappings.userSuppliedDestinationMappings(of("/destination", "Destination")));
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
        @ConditionalOnMissingBean(name = "defaultSourceNameMapping")
        public SourceNameMappings defaultSourceNameMapping() {
            return UserSuppliedSourceMappings.userSuppliedSourceMappings(of("/source", "Source"));
        }

        @Bean
        public DestinationNameMappings defaultDestinationNameMapping() {
            return UserSuppliedDestinationMappings.userSuppliedDestinationMappings(of("/destination", "Destination"));
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
