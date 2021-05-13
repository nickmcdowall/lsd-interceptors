package com.nickmcdowall.lsd.http.interceptor;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class SpringDataRepositoryInterceptorTest {

    private AopInterceptorDelegate delegate = mock(AopInterceptorDelegate.class);
    private SpringDataRepositoryInterceptor interceptor = new SpringDataRepositoryInterceptor(delegate);

    @Test
    void trapExceptionsToPreventBreakingBuilds() {
        interceptor.captureRepositoryResponses(null, null);
        interceptor.captureRepositoryErrors(null, null);
    }
}