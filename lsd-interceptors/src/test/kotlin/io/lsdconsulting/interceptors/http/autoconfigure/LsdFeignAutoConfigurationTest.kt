package io.lsdconsulting.interceptors.http.autoconfigure

import feign.Logger
import io.lsdconsulting.interceptors.http.LsdFeignLoggerInterceptor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

internal class LsdFeignAutoConfigurationTest {
    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                LsdFeignAutoConfiguration::class.java
            )
        )

    @Test
    fun loadsBeansWhenTestStateIsAvailable() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans::class.java)
            .run { context: AssertableApplicationContext? ->
                assertThat(context).hasBean("defaultSourceNameMapping")
                assertThat(context).hasBean("defaultDestinationNameMapping")
                assertThat(context).hasBean("httpInteractionHandlers")
                assertThat(context).hasSingleBean(
                    Logger.Level::class.java
                )
                assertThat(context).hasSingleBean(
                    LsdFeignLoggerInterceptor::class.java
                )
            }
    }

    @Test
    fun noBeansAutoLoadedWhenInterceptorsDisabledViaPropertyEvenIfBeansAvailable() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans::class.java)
            .withPropertyValues("lsd.interceptors.autoconfig.enabled=false")
            .run { context: AssertableApplicationContext -> assertBeansNotLoaded(context) }
    }

    @Test
    fun doesntReplaceExistingLoggerBean() {
        contextRunner.withUserConfiguration(UserConfigWithExistingLoggerBean::class.java)
            .run { context: AssertableApplicationContext? ->
                assertThat(context).getBean(Logger.Level::class.java)
                    .isEqualTo(UserConfigWithExistingLoggerBean().feignLoggerLevel())
            }
    }

    private fun assertBeansNotLoaded(context: AssertableApplicationContext) {
        assertThat(context).doesNotHaveBean("defaultSourceNameMapping")
        assertThat(context).doesNotHaveBean("defaultDestinationNameMapping")
        assertThat(context).doesNotHaveBean("httpInteractionHandlers")
        assertThat(context).doesNotHaveBean(Logger.Level::class.java)
        assertThat(context).doesNotHaveBean(LsdFeignLoggerInterceptor::class.java)
    }

    @Configuration
    internal open class UserConfigWithoutRequiredBeans

    @Configuration
    internal open class UserConfigWithRequiredBeans {
        /*
         * To catch autoconfig beans of type List (the generic type is not taken into account, so we need to use a name
         * or wrapper type for the collection
         */
        @Bean
        open fun genericList(): List<Any> {
            return listOf()
        }
    }

    internal class UserConfigWithExistingLoggerBean {
        @Bean
        fun feignLoggerLevel(): Logger.Level {
            return Logger.Level.FULL
        }
    }
}
