package io.lsdconsulting.interceptors.http.autoconfigure

import com.lsd.core.LsdContext
import io.lsdconsulting.interceptors.common.AppName.Factory.create
import io.lsdconsulting.interceptors.http.LsdOkHttpInterceptor
import io.lsdconsulting.interceptors.http.common.DefaultHttpInteractionHandler
import io.lsdconsulting.interceptors.http.naming.AlwaysAppName
import io.lsdconsulting.interceptors.http.naming.RegexResolvingNameMapper
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class LsdOkHttpAutoConfigurationTest {
    private val lsdContext: LsdContext = LsdContext.instance
    private val alwaysAppName = AlwaysAppName(create("App"))
    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                LsdOkHttpAutoConfiguration::class.java
            )
        )

    @Test
    fun noBeansAutoLoadedWhenRequiredBeansMissing() {
        contextRunner.withUserConfiguration(UserConfigWithoutRequiredBeans::class.java)
            .withPropertyValues("lsd.interceptors.autoconfig.okhttp.enabled=true")
            .run { context: AssertableApplicationContext? ->
                assertThat(context).doesNotHaveBean("defaultSourceNameMapping")
                assertThat(context).doesNotHaveBean("defaultDestinationNameMapping")
                assertThat(context).doesNotHaveBean("httpInteractionHandlers")
                assertThat(context).doesNotHaveBean(OkHttpClient.Builder::class.java)
            }
    }

    @Test
    fun addsOkHttpBuilderWhenPropertySetAndRequiredBeansAvailable() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans::class.java)
            .withPropertyValues("lsd.interceptors.autoconfig.okhttp.enabled=true")
            .run { context: AssertableApplicationContext ->
                assertThat(context).hasBean("defaultSourceNameMapping")
                assertThat(context).hasBean("defaultDestinationNameMapping")
                assertThat(context).hasBean("httpInteractionHandlers")
                assertThat(context).hasSingleBean(OkHttpClient.Builder::class.java)
                assertThat(
                    context.getBean(OkHttpClient.Builder::class.java).interceptors()
                ).containsExactly(
                    LsdOkHttpInterceptor(
                        listOf(
                            DefaultHttpInteractionHandler(
                                lsdContext,
                                alwaysAppName,
                                RegexResolvingNameMapper()
                            )
                        )
                    )
                )
            }
    }

    @Test
    fun noInterceptorWhenPropertyNotSet() {
        contextRunner.withUserConfiguration(UserConfigWithRequiredBeans::class.java)
            .run { context: AssertableApplicationContext ->
                assertThat(context).doesNotHaveBean("defaultSourceNameMapping")
                assertThat(context).doesNotHaveBean("defaultDestinationNameMapping")
                assertThat(context).doesNotHaveBean("httpInteractionHandlers")
                assertThat(context.getBean(OkHttpClient.Builder::class.java).interceptors()).isEmpty()
            }
    }

    @Configuration
    internal open class UserConfigWithoutRequiredBeans

    @Configuration
    internal open class UserConfigWithRequiredBeans {
        @Bean
        open fun httpClient(): OkHttpClient.Builder {
            return OkHttpClient.Builder()
        }

        /*
         * To catch autoconfig beans of type List (the generic type is not taken into account so we need to use a name
         * or wrapper type for the collection
         */
        @Bean
        open fun genericList(): List<Any> {
            return listOf()
        }
    }
}
