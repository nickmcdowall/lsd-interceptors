package com.nickmcdowall.lsd.http.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.common.DefaultHttpInteractionHandler;
import com.nickmcdowall.lsd.http.interceptor.LsdOkHttpInterceptor;
import com.nickmcdowall.lsd.http.naming.RegexResolvingNameMapper;
import com.nickmcdowall.lsd.http.naming.AppName;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
                    assertThat(context).doesNotHaveBean("httpInteractionHandlers");
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
                    assertThat(context).hasBean("httpInteractionHandlers");
                    assertThat(context).hasSingleBean(OkHttpClient.Builder.class);
                    assertThat(context.getBean(OkHttpClient.Builder.class).interceptors()).containsExactly(
                            new LsdOkHttpInterceptor(List.of(new DefaultHttpInteractionHandler(new TestState(), new AppName("App"), new RegexResolvingNameMapper()))));
                });
    }

    @Test
    public void noInterceptorWhenPropertyNotSet() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans.class).run((context) -> {
            assertThat(context).doesNotHaveBean("defaultSourceNameMapping");
            assertThat(context).doesNotHaveBean("defaultDestinationNameMapping");
            assertThat(context).doesNotHaveBean("httpInteractionHandlers");
            assertThat(context.getBean(OkHttpClient.Builder.class).interceptors()).isEmpty();
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

        /*
         * To catch autoconfig beans of type List (the generic type is not taken into account so we need to use a name
         * or wrapper type for the collection
         */
        @Bean
        public List<Object> genericList() {
            return List.of();
        }
    }
}
