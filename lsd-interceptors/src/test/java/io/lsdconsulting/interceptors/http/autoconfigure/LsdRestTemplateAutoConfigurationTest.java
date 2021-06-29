package io.lsdconsulting.interceptors.http.autoconfigure;

import com.lsd.LsdContext;
import io.lsdconsulting.interceptors.common.AppName;
import io.lsdconsulting.interceptors.http.LsdRestTemplateCustomizer;
import io.lsdconsulting.interceptors.http.LsdRestTemplateInterceptor;
import io.lsdconsulting.interceptors.http.common.DefaultHttpInteractionHandler;
import io.lsdconsulting.interceptors.http.naming.AlwaysAppName;
import io.lsdconsulting.interceptors.http.naming.RegexResolvingNameMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LsdRestTemplateAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LsdRestTemplateAutoConfiguration.class
            ));
    private AlwaysAppName alwaysAppName = new AlwaysAppName(new AppName("App"));

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
            assertThat(context).hasBean("httpInteractionHandlers");
            assertThat(context).getBean(LsdRestTemplateCustomizer.class).isEqualTo(
                    new LsdRestTemplateCustomizer(new LsdRestTemplateInterceptor(
                            List.of(new DefaultHttpInteractionHandler(LsdContext.getInstance(), alwaysAppName, new RegexResolvingNameMapper())))));

        });
    }

    @Test
    void noBeansWhenDisabledViaProperty() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans.class)
                .withPropertyValues("lsd.interceptors.autoconfig.enabled=false")
                .run((context) -> {
                    assertThat(context).doesNotHaveBean("defaultSourceNameMapping");
                    assertThat(context).doesNotHaveBean("defaultDestinationNameMapping");
                    assertThat(context).doesNotHaveBean("lsdRestTemplateInterceptor");
                    assertThat(context).doesNotHaveBean("httpInteractionHandlers");
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
