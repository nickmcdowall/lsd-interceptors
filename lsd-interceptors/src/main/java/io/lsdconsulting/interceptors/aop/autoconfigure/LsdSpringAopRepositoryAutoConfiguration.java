package io.lsdconsulting.interceptors.aop.autoconfigure;

import com.lsd.core.LsdContext;
import io.lsdconsulting.interceptors.aop.AopInterceptorDelegate;
import io.lsdconsulting.interceptors.aop.SpringDataRepositoryInterceptor;
import io.lsdconsulting.interceptors.common.AppName;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.repository.Repository;

import javax.annotation.PostConstruct;
import java.util.Set;


/**
 * <p>
 * If a {@link Repository} class, {@link LsdContext} class and AOP dependencies are available it will automatically
 * intercept calls to the repository as well as exceptions thrown
 * </p>
 */
@Configuration
@ConditionalOnProperty(name = "lsd.interceptors.autoconfig.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(value = {LsdContext.class, Repository.class})
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class LsdSpringAopRepositoryAutoConfiguration {

    private final LsdContext lsdContext = LsdContext.getInstance();

    @Value("${info.app.name:App}")
    private String appName;

    @Bean
    public SpringDataRepositoryInterceptor springDataRepositoryInterceptor(AopInterceptorDelegate aopInterceptorDelegate) {
        return new SpringDataRepositoryInterceptor(aopInterceptorDelegate);
    }

    @Bean
    public AopInterceptorDelegate aopInterceptorDelegate() {
        return new AopInterceptorDelegate(lsdContext, AppName.Factory.create(appName));
    }

    @PostConstruct
    private void postConstruct() {
        lsdContext.includeFiles(
                Set.of("tupadr3/font-awesome-5/database",
                        "tupadr3/font-awesome-5/clock")
        );
    }
}
