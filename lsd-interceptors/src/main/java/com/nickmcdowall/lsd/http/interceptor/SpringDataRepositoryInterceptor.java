package com.nickmcdowall.lsd.http.interceptor;

import com.nickmcdowall.lsd.repository.interceptor.AopInterceptorDelegate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class SpringDataRepositoryInterceptor {
    private final AopInterceptorDelegate delegate;

    @AfterReturning(value = "within(org.springframework.data.repository.Repository+)) || within(@org.springframework.stereotype.Repository *+)", returning = "resultValue")
    public void captureRepositoryResponses(JoinPoint joinPoint, Object resultValue) {
        try {
            if (isMockitoWrapper(joinPoint))
                return;
            delegate.captureInternalInteraction(joinPoint, resultValue, "<$database{scale=0.4,color=grey}>");
        } catch (Exception e) {
            log.error("Failed while intercepting repository call for LSD", e);
        }
    }

    @AfterThrowing(value = "within(org.springframework.data.repository.Repository+) || within(@org.springframework.stereotype.Repository *+)", throwing = "throwable")
    public void captureRepositoryErrors(JoinPoint joinPoint, Throwable throwable) {
        try {
            if (isMockitoWrapper(joinPoint))
                return;
            delegate.captureInternalException(throwable, "<$database{scale=0.4,color=red}>");
        } catch (Exception e) {
            log.error("Failed while intercepting repository exception for LSD", e);
        }
    }

    /*
     * If the repository is a mockito spy we end up with a duplicate joinpoint being created so we filter them out
     */
    private boolean isMockitoWrapper(JoinPoint joinpoint) {
        return joinpoint.getSignature().getDeclaringType().getName().contains("MockitoMock");
    }
}
