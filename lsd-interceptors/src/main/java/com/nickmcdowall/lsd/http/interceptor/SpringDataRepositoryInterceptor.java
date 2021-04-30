package com.nickmcdowall.lsd.http.interceptor;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@RequiredArgsConstructor
public class SpringDataRepositoryInterceptor {
    private final AopInterceptorDelegate delegate;

    @AfterReturning(value = "execution(* org.springframework.data.repository.Repository+.*(*)))", returning = "resultValue")
    public void captureRepositoryResponses(JoinPoint joinPoint, Object resultValue) {
        if (isMockitoWrapper(joinPoint))
            return;
        delegate.logInternalResponse(resultValue, joinPoint);
    }

    @AfterThrowing(value = "execution(* org.springframework.data.repository.Repository+.*(*))", throwing = "throwable")
    public void captureRepositoryErrors(JoinPoint joinPoint, Throwable throwable) {
        if (isMockitoWrapper(joinPoint))
            return;
        delegate.logInternalException(throwable);
    }

    /*
     * If the repository is a mockito spy we end up with a duplicate joinpoint being created so we filter them out
     */
    private boolean isMockitoWrapper(JoinPoint joinpoint) {
        return joinpoint.getSignature().getDeclaringType().getName().contains("MockitoMock");
    }
}
