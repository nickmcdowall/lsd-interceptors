package io.lsdconsulting.interceptors.http.autoconfigure;

import com.lsd.LsdContext;
import io.lsdconsulting.interceptors.common.AppName;
import io.lsdconsulting.interceptors.http.LsdOkHttpInterceptor;
import io.lsdconsulting.interceptors.http.common.DefaultHttpInteractionHandler;
import io.lsdconsulting.interceptors.http.naming.AlwaysAppName;
import io.lsdconsulting.interceptors.http.naming.RegexResolvingNameMapper;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LsdOkHttpAutoConfigurationTest {

    private final LsdContext lsdContext = LsdContext.getInstance();
    private final AlwaysAppName alwaysAppName = new AlwaysAppName(new AppName("App"));

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LsdOkHttpAutoConfiguration.class
            ));

    @Test
    public void noBeansAutoLoadedWhenRequiredBeansMissing() {
        contextRunner.withUserConfiguration(UserConfigWithoutRequiredBeans.class)
                .withPropertyValues("lsd.interceptors.autoconfig.okhttp.enabled=true")
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
                .withPropertyValues("lsd.interceptors.autoconfig.okhttp.enabled=true")
                .run((context) -> {
                    assertThat(context).hasBean("defaultSourceNameMapping");
                    assertThat(context).hasBean("defaultDestinationNameMapping");
                    assertThat(context).hasBean("httpInteractionHandlers");
                    assertThat(context).hasSingleBean(OkHttpClient.Builder.class);
                    assertThat(context.getBean(OkHttpClient.Builder.class).interceptors()).containsExactly(
                            new LsdOkHttpInterceptor(List.of(new DefaultHttpInteractionHandler(lsdContext, alwaysAppName, new RegexResolvingNameMapper()))));
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
