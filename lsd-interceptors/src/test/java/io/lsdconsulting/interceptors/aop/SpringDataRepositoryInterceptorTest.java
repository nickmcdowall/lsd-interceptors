package io.lsdconsulting.interceptors.aop;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class SpringDataRepositoryInterceptorTest {

    private final AopInterceptorDelegate delegate = mock(AopInterceptorDelegate.class);
    private final SpringDataRepositoryInterceptor interceptor = new SpringDataRepositoryInterceptor(delegate);

    @Test
    void trapExceptionsToPreventBreakingBuilds() {
        interceptor.captureRepositoryResponses(null, null);
        interceptor.captureRepositoryErrors(null, null);
    }
}