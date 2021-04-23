package com.nickmcdowall.lsd.http.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.interceptor.LsdFeignLoggerInterceptor;
import feign.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LsdFeignAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LsdFeignAutoConfiguration.class
            ));

    @Test
    public void noBeansAutoLoadedWhenRequiredBeansMissing() {
        contextRunner.withUserConfiguration(UserConfigWithoutRequiredBeans.class)
                .run((context) -> assertBeansNotLoaded(context));
    }

    @Test
    void loadsBeansWhenTestStateIsAvailable() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans.class).run((context) -> {
            assertThat(context).hasBean("defaultSourceNameMapping");
            assertThat(context).hasBean("defaultDestinationNameMapping");
            assertThat(context).hasBean("httpInteractionHandlers");
            assertThat(context).hasSingleBean(Logger.Level.class);
            assertThat(context).hasSingleBean(LsdFeignLoggerInterceptor.class);
        });
    }

    @Test
    void noBeansAutoLoadedWhenInterceptorsDisabledViaPropertyEvenIfBeansAvailable() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans.class)
                .withPropertyValues("yatspec.lsd.interceptors.autoconfig.enabled=false")
                .run((context) -> assertBeansNotLoaded(context));
    }

    @Test
    void doesntReplaceExistingLoggerBean() {
        contextRunner.withUserConfiguration(UserConfigWithExistingLoggerBean.class).run((context) -> {
            assertThat(context).getBean(Logger.Level.class)
                    .isEqualTo(new UserConfigWithExistingLoggerBean().feignLoggerLevel());
        });
    }

    private void assertBeansNotLoaded(AssertableApplicationContext context) {
        assertThat(context).doesNotHaveBean("defaultSourceNameMapping");
        assertThat(context).doesNotHaveBean("defaultDestinationNameMapping");
        assertThat(context).doesNotHaveBean("httpInteractionHandlers");
        assertThat(context).doesNotHaveBean(Logger.Level.class);
        assertThat(context).doesNotHaveBean(LsdFeignLoggerInterceptor.class);
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

        /*
         * To catch autoconfig beans of type List (the generic type is not taken into account so we need to use a name
         * or wrapper type for the collection
         */
        @Bean
        public List<Object> genericList() {
            return List.of();
        }
    }

    static class UserConfigWithExistingLoggerBean {
        @Bean
        public TestState interactions() {
            return new TestState();
        }

        @Bean
        public Logger.Level feignLoggerLevel() {
            return Logger.Level.FULL;
        }
    }
}