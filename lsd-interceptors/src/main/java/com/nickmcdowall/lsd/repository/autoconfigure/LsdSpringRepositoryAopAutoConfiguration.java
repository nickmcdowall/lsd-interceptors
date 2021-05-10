package com.nickmcdowall.lsd.repository.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.interceptor.AopInterceptorDelegate;
import com.nickmcdowall.lsd.http.interceptor.SpringDataRepositoryInterceptor;
import com.nickmcdowall.lsd.http.naming.AppName;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.repository.Repository;

import javax.annotation.PostConstruct;


/**
 * <p>
 * If a {@link Repository} class, a {@link TestState} bean is available and AOP dependencies are available it will automatically
 * intercept calls to the repository as well as exceptions thrown
 * </p>
 */
@Configuration
@ConditionalOnProperty(name = "yatspec.lsd.interceptors.autoconfig.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(value = {TestState.class})
@ConditionalOnClass(value = {Repository.class})
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class LsdSpringRepositoryAopAutoConfiguration {

    private final TestState testState;

    @Value("${info.app.name:App}")
    private String appName;

    @Bean
    public SpringDataRepositoryInterceptor springDataRepositoryInterceptor() {
        return new SpringDataRepositoryInterceptor(new AopInterceptorDelegate(testState, new AppName(appName)));
    }

    @PostConstruct
    private void postConstruct() {
        testState.include("tupadr3/font-awesome-5/database");
    }
}
