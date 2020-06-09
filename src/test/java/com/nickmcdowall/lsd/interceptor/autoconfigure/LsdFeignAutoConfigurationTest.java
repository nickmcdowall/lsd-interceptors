package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.PathToNameMapper;
import com.nickmcdowall.lsd.interceptor.rest.LsdFeignLoggerInterceptor;
import feign.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.nickmcdowall.lsd.interceptor.common.UserSuppliedMappings.userSuppliedMappings;
import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;

class LsdFeignAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LsdFeignAutoConfiguration.class
            ));

    @Test
    public void noBeansAutoLoadedWhenRequiredBeansMissing() {
        contextRunner.withUserConfiguration(UserConfigWithoutRequiredBeans.class).run((context) -> {
            assertThat(context).doesNotHaveBean("defaultFeignSourceNameMapping");
            assertThat(context).doesNotHaveBean("defaultFeignDestinationNameMapping");
            assertThat(context).doesNotHaveBean(Logger.Level.class);
            assertThat(context).doesNotHaveBean(LsdFeignLoggerInterceptor.class);
        });
    }

    @Test
    void loadsBeansWhenTestStateIsAvailable() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans.class).run((context) -> {
            assertThat(context).hasBean("defaultFeignSourceNameMapping");
            assertThat(context).hasBean("defaultFeignDestinationNameMapping");
            assertThat(context).hasSingleBean(Logger.Level.class);
            assertThat(context).hasSingleBean(LsdFeignLoggerInterceptor.class);
        });
    }

    @Test
    void doesntReplaceExistingLoggerBean() {
        contextRunner.withUserConfiguration(UserConfigWithExistingLoggerBean.class).run((context) -> {
            assertThat(context).getBean(Logger.Level.class)
                    .isEqualTo(new UserConfigWithExistingLoggerBean().feignLoggerLevel());
        });
    }

    @Test
    void userCanOverrideNameMappings() {
        contextRunner.withUserConfiguration(UserConfigWithNameMappingOverrides.class).run((context) -> {
            assertThat(context).getBean("defaultFeignSourceNameMapping", PathToNameMapper.class)
                    .isEqualTo(userSuppliedMappings(of("/source", "Source")));
            assertThat(context).getBean("defaultFeignDestinationNameMapping", PathToNameMapper.class)
                    .isEqualTo(userSuppliedMappings(of("/destination", "Destination")));
        });
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
    }

    @Configuration
    static class UserConfigWithNameMappingOverrides {
        @Bean
        public TestState interactions() {
            return new TestState();
        }

        @Bean
        public PathToNameMapper defaultFeignSourceNameMapping() {
            return userSuppliedMappings(of("/source", "Source"));
        }

        @Bean
        public PathToNameMapper defaultFeignDestinationNameMapping() {
            return userSuppliedMappings(of("/destination", "Destination"));
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