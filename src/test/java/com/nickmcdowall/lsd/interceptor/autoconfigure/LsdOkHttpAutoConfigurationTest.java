package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.RegexResolvingDestinationNameMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdOkHttpInterceptor;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class LsdOkHttpAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LsdOkHttpAutoConfiguration.class,
                    LsdDestinationNameMappingConfiguration.class
            ));

    @Test
    public void addsOkHttpBuilderWhen() {
        contextRunner.withUserConfiguration(UserConfigWithTestState.class).run((context) -> {
            assertThat(context).hasSingleBean(OkHttpClient.Builder.class);
            assertThat(context.getBean(OkHttpClient.Builder.class).interceptors()).containsExactly(new LsdOkHttpInterceptor(new TestState(), "App", new RegexResolvingDestinationNameMapper()));
        });
    }

    @Configuration
    static class UserConfigWithTestState {
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
