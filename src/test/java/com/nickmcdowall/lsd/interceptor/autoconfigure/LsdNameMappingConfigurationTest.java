package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.PathToNameMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.autoconfigure.AutoConfigurations.of;

class LsdNameMappingConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(of(LsdNameMappingConfiguration.class));

    @Test
    public void autoConfigAddsDefaultMappingBeansForInterceptors() {
        contextRunner.withUserConfiguration(UserConfigWithNoPathToNameMapperBeans.class).run((context) -> {
            assertThat(context).hasBean("defaultOkHttpSourceNameMapping");
            assertThat(context).hasBean("defaultOkHttpDestinationNameMapping");
            assertThat(context).hasBean("defaultRestTemplateSourceNameMapping");
            assertThat(context).hasBean("defaultRestTemplateDestinationNameMapping");
            assertThat(context).hasBean("defaultTestRestTemplateSourceNameMapping");
            assertThat(context).hasBean("defaultTestRestTemplateDestinationNameMapping");
        });
    }

    @Test
    public void userBeansOverrideAutoConfigDefaults() {
        UserConfigWithUserPathToNameMapperBeans userConfig = new UserConfigWithUserPathToNameMapperBeans();
        contextRunner.withUserConfiguration(userConfig.getClass()).run((context) -> {
            assertThat(context).getBean("defaultOkHttpSourceNameMapping").isEqualTo(userConfig.defaultOkHttpSourceNameMapping());
            assertThat(context).getBean("defaultOkHttpDestinationNameMapping").isEqualTo(userConfig.defaultOkHttpDestinationNameMapping());
            assertThat(context).getBean("defaultRestTemplateSourceNameMapping").isEqualTo(userConfig.defaultRestTemplateSourceNameMapping());
            assertThat(context).getBean("defaultRestTemplateDestinationNameMapping").isEqualTo(userConfig.defaultRestTemplateDestinationNameMapping());
            assertThat(context).getBean("defaultTestRestTemplateSourceNameMapping").isEqualTo(userConfig.defaultTestRestTemplateSourceNameMapping());
            assertThat(context).getBean("defaultTestRestTemplateDestinationNameMapping").isEqualTo(userConfig.defaultTestRestTemplateDestinationNameMapping());
        });
    }

    @Configuration
    static class UserConfigWithNoPathToNameMapperBeans {
        @Bean
        public TestState testState() {
            return new TestState();
        }
    }


    @Configuration
    static class UserConfigWithUserPathToNameMapperBeans {

        public static final PathToNameMapper OK_HTTP_DESTINATION_MAPPING = path -> "aName1";
        public static final PathToNameMapper OK_HTTP_SOURCE_MAPPING = path -> "aName2";
        public static final PathToNameMapper REST_TEMPLATE_SOURCE_MAPPING = path -> "aName3";
        public static final PathToNameMapper REST_TEMPLATE_DESTINATION_MAPPING = path -> "aName4";
        public static final PathToNameMapper TEST_REST_TEMPLATE_SOURCE_MAPPING = path -> "aName5";
        public static final PathToNameMapper TEST_REST_TEMPLATE_DESTINATION_MAPPING = path -> "aName6";

        @Bean
        public TestState testState() {
            return new TestState();
        }

        @Bean
        public PathToNameMapper defaultOkHttpSourceNameMapping() {
            return OK_HTTP_SOURCE_MAPPING;
        }

        @Bean
        public PathToNameMapper defaultOkHttpDestinationNameMapping() {
            return OK_HTTP_DESTINATION_MAPPING;
        }

        @Bean
        public PathToNameMapper defaultRestTemplateDestinationNameMapping() {
            return REST_TEMPLATE_DESTINATION_MAPPING;
        }

        @Bean
        public PathToNameMapper defaultRestTemplateSourceNameMapping() {
            return REST_TEMPLATE_SOURCE_MAPPING;
        }

        @Bean
        public PathToNameMapper defaultTestRestTemplateSourceNameMapping() {
            return TEST_REST_TEMPLATE_SOURCE_MAPPING;
        }

        @Bean
        public PathToNameMapper defaultTestRestTemplateDestinationNameMapping() {
            return TEST_REST_TEMPLATE_DESTINATION_MAPPING;
        }
    }
}