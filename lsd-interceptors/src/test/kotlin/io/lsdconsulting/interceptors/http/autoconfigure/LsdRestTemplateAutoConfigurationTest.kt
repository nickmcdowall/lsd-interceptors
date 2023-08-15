package io.lsdconsulting.interceptors.http.autoconfigure

import com.lsd.core.LsdContext
import io.lsdconsulting.interceptors.common.AppName.Factory.create
import io.lsdconsulting.interceptors.http.LsdRestTemplateCustomizer
import io.lsdconsulting.interceptors.http.LsdRestTemplateInterceptor
import io.lsdconsulting.interceptors.http.common.DefaultHttpInteractionHandler
import io.lsdconsulting.interceptors.http.naming.AlwaysAppName
import io.lsdconsulting.interceptors.http.naming.RegexResolvingNameMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.assertj.ApplicationContextAssert
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

internal class LsdRestTemplateAutoConfigurationTest {
    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                LsdRestTemplateAutoConfiguration::class.java
            )
        )
    private val alwaysAppName = AlwaysAppName(create("App"))

    @Test
    fun noInterceptorAddedWhenNoTestStateBeanExists() {
        contextRunner.withUserConfiguration(UserConfigWithNoTestState::class.java)
            .run { context: AssertableApplicationContext ->
                assertThat(context).hasSingleBean(
                    RestTemplate::class.java
                )
                assertThat(
                    context.getBean(
                        RestTemplate::class.java
                    ).interceptors
                ).isEmpty()
            }
    }

    @Test
    fun restTemplateInterceptorAdded() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans::class.java)
            .run { context: AssertableApplicationContext? ->
                assertThat(context).hasSingleBean(
                    RestTemplate::class.java
                )
                assertThat(context).hasBean("defaultSourceNameMapping")
                assertThat(context).hasBean("defaultDestinationNameMapping")
                assertThat(context).hasBean("httpInteractionHandlers")
                assertThat<ApplicationContextAssert<ConfigurableApplicationContext>>(context)
                    .getBean(
                        LsdRestTemplateCustomizer::class.java
                    ).isEqualTo(
                    LsdRestTemplateCustomizer(
                        LsdRestTemplateInterceptor(
                            listOf(
                                DefaultHttpInteractionHandler(
                                    LsdContext.instance,
                                    alwaysAppName,
                                    RegexResolvingNameMapper()
                                )
                            )
                        )
                    )
                )
            }
    }

    @Test
    fun noBeansWhenDisabledViaProperty() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans::class.java)
            .withPropertyValues("lsd.interceptors.autoconfig.enabled=false")
            .run { context: AssertableApplicationContext? ->
                assertThat(context).doesNotHaveBean("defaultSourceNameMapping")
                assertThat(context).doesNotHaveBean("defaultDestinationNameMapping")
                assertThat(context).doesNotHaveBean("lsdRestTemplateInterceptor")
                assertThat(context).doesNotHaveBean("httpInteractionHandlers")
            }
    }

    @Configuration
    internal open class UserConfigWithoutRequiredBeans

    @Configuration
    internal open class UserConfigWithNoTestState {
        @Bean
        open fun myRestTemplate(): RestTemplate {
            return RestTemplate()
        }
    }

    @Configuration
    internal open class UserConfigWithRequiredBeans {
        @Bean
        open fun myRestTemplate(): RestTemplate {
            return RestTemplate()
        }

        /*
         * To catch autoconfig beans of type List (the generic type is not taken into account, so we need to use a name
         * or wrapper type for the collection
         */
        @Bean
        open fun genericList(): List<Any> {
            return listOf()
        }
    }
}
