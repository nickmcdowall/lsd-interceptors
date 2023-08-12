package io.lsdconsulting.interceptors.aop.autoconfigure

import com.lsd.core.LsdContext
import io.lsdconsulting.interceptors.aop.AopInterceptorDelegate
import io.lsdconsulting.interceptors.aop.SpringDataRepositoryInterceptor
import io.lsdconsulting.interceptors.common.AppName.Factory.create
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.data.repository.Repository
import javax.annotation.PostConstruct

/**
 *
 *
 * If a [Repository] class, [LsdContext] class and AOP dependencies are available it will automatically
 * intercept calls to the repository as well as exceptions thrown
 *
 */
@Configuration
@ConditionalOnProperty(name = ["lsd.interceptors.autoconfig.enabled"], havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(value = [LsdContext::class, Repository::class])
@EnableAspectJAutoProxy
open class LsdSpringAopRepositoryAutoConfiguration {
    private val lsdContext: LsdContext = LsdContext.instance

    @Value("\${info.app.name:App}")
    private lateinit var appName: String

    @Bean
    open fun springDataRepositoryInterceptor(aopInterceptorDelegate: AopInterceptorDelegate): SpringDataRepositoryInterceptor {
        return SpringDataRepositoryInterceptor(aopInterceptorDelegate)
    }

    @Bean
    open fun aopInterceptorDelegate(): AopInterceptorDelegate {
        return AopInterceptorDelegate(lsdContext, create(appName))
    }

    @PostConstruct
    private fun postConstruct() {
        lsdContext.includeFiles(
            setOf(
                "tupadr3/font-awesome-5/database",
                "tupadr3/font-awesome-5/clock"
            )
        )
    }
}
