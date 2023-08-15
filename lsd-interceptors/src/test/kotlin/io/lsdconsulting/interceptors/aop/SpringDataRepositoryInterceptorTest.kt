package io.lsdconsulting.interceptors.aop

import io.mockk.mockk
import org.aspectj.lang.JoinPoint
import org.junit.jupiter.api.Test

internal class SpringDataRepositoryInterceptorTest {
    private val delegate = mockk<AopInterceptorDelegate>()
    private val interceptor = SpringDataRepositoryInterceptor(delegate)

    @Test
    fun trapExceptionsToPreventBreakingBuilds() {
        interceptor.captureRepositoryResponses(mockk<JoinPoint>(), null)
        interceptor.captureRepositoryErrors(mockk<JoinPoint>(), Exception())
    }
}
