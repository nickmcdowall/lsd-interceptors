package io.lsdconsulting.interceptors.aop

import io.lsdconsulting.interceptors.common.log
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import java.time.ZonedDateTime

@Aspect
class SpringDataRepositoryInterceptor(
    private val delegate: AopInterceptorDelegate
) {

    @AfterReturning(
        value = "within(org.springframework.data.repository.Repository+) || within(@org.springframework.stereotype.Repository *+)",
        returning = "resultValue"
    )
    fun captureRepositoryResponses(joinPoint: JoinPoint, resultValue: Any?) {
        try {
            if (isMockitoWrapper(joinPoint)) return
            delegate.captureInternalInteraction(joinPoint, resultValue, "<\$database{scale=0.4,color=grey}>")
        } catch (e: Exception) {
            log().error("Failed while intercepting repository call for LSD", e)
        }
    }

    @AfterThrowing(
        value = "within(org.springframework.data.repository.Repository+) || within(@org.springframework.stereotype.Repository *+)",
        throwing = "throwable"
    )
    fun captureRepositoryErrors(joinPoint: JoinPoint, throwable: Throwable) {
        try {
            if (isMockitoWrapper(joinPoint)) return
            delegate.captureInternalException(joinPoint, throwable, "<\$database{scale=0.4,color=red}>")
        } catch (e: Exception) {
            log().error("Failed while intercepting repository exception for LSD", e)
        }
    }

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    @Throws(Throwable::class)
    fun captureScheduledMethods(joinPoint: ProceedingJoinPoint) {
        val startTime = ZonedDateTime.now()
        safely { delegate.captureScheduledStart(joinPoint, startTime) }
        try {
            joinPoint.proceed()
        } catch (e: Throwable) {
            safely { delegate.captureScheduledError(joinPoint, startTime, ZonedDateTime.now(), e) }
            throw e
        } finally {
            safely { delegate.captureScheduledEnd(joinPoint, startTime, ZonedDateTime.now()) }
        }
    }

    fun safely(runnable: Runnable) {
        try {
            runnable.run()
        } catch (t: Throwable) {
            log().error("LSD interceptor exception while intercepting AOP execution", t)
        }
    }

    /*
     * If the repository is a mockito spy we end up with a duplicate joinpoint being created so we filter them out
     */
    private fun isMockitoWrapper(joinpoint: JoinPoint): Boolean {
        return joinpoint.signature.declaringType.name.contains("MockitoMock")
    }
}
