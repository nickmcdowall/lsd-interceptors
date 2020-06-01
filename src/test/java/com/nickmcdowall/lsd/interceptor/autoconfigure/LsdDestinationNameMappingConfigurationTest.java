package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.interceptor.common.DestinationNamesMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class LsdDestinationNameMappingConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LsdDestinationNameMappingConfiguration.class
            ));

    @Test
    public void aDestinationsBeanIsAddedWhenNoneExists() {
        contextRunner.withUserConfiguration(UserConfigWithNoDestinationMapping.class).run((context) -> {
            assertThat(context).hasSingleBean(DestinationNamesMapper.class);
        });
    }

    @Configuration
    static class UserConfigWithNoDestinationMapping {
        @Bean
        public TestState testState() {
            return new TestState();
        }
    }
}