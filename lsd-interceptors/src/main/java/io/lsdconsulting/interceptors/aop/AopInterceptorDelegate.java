package io.lsdconsulting.interceptors.aop;

import com.lsd.core.LsdContext;
import io.lsdconsulting.interceptors.common.AppName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lsd.format.PrettyPrinter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

import java.time.ZonedDateTime;

import static com.lsd.core.builders.ActivateLifelineBuilder.activation;
import static com.lsd.core.builders.DeactivateLifelineBuilder.deactivation;
import static com.lsd.core.builders.MessageBuilder.messageBuilder;
import static com.lsd.core.domain.MessageType.SHORT_INBOUND;
import static com.lsd.core.domain.MessageType.SYNCHRONOUS_RESPONSE;
import static j2html.TagCreator.*;
import static java.lang.System.lineSeparator;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

@Slf4j
@RequiredArgsConstructor
public class AopInterceptorDelegate {
    private final LsdContext lsdContext;
    private final AppName appName;

    public void captureInternalInteraction(JoinPoint joinPoint, Object resultValue, String icon) {
        captureInteraction(joinPoint, resultValue, appName.getValue(), appName.getValue(), icon);
    }

    public void captureInteraction(JoinPoint joinPoint, Object resultValue, String sourceName, String destinationName, String icon) {
        lsdContext.capture(messageBuilder()
                .id(lsdContext.getIdGenerator().next())
                .from(sourceName)
                .to(destinationName)
                .label(icon + " " + joinPoint.getSignature().toShortString())
                .data(renderHtmlForMethodCall(joinPoint.getArgs(), resultValue))
                .build());
    }

    public void captureInternalException(JoinPoint joinPoint, Throwable throwable, String icon) {
        captureException(joinPoint, throwable, appName.getValue(), appName.getValue(), icon);
    }

    public void captureException(JoinPoint joinPoint, Throwable throwable, String sourceName, String destinationName, String icon) {
        lsdContext.capture(messageBuilder()
                .id(lsdContext.getIdGenerator().next())
                .from(sourceName)
                .to(destinationName)
                .label(icon + " " + throwable.getClass().getSimpleName())
                .type(SYNCHRONOUS_RESPONSE)
                .colour("red")
                .data(renderHtmlForException(joinPoint.getSignature().toShortString(), joinPoint.getArgs(), throwable))
                .build());
    }

    public void captureScheduledStart(ProceedingJoinPoint joinPoint, ZonedDateTime startTime) {
        lsdContext.capture(messageBuilder()
                .id(lsdContext.getIdGenerator().next())
                .to(appName.getValue())
                .label("<$clock{scale=0.3}> ")
                .type(SHORT_INBOUND)
                .data(p(
                        p(
                                h4("Scheduled"),
                                code(joinPoint.getSignature().toShortString())
                        ),
                        p(
                                h4("Timestamp"),
                                code(startTime.format(ISO_DATE_TIME))
                        )).render())
                .build());
        lsdContext.capture(activation().of(appName.getValue()).colour("skyblue").build());
    }

    public void captureScheduledEnd(ProceedingJoinPoint joinPoint, ZonedDateTime startTime, ZonedDateTime endTime) {
        String delay = MILLIS.between(startTime, endTime) + "ms";
        lsdContext.capture(messageBuilder()
                .id(lsdContext.getIdGenerator().next())
                .to(appName.getValue())
                .label("<$clock{scale=0.3}>")
                .data(
                        p(
                                p(
                                        h4("Scheduler completed"),
                                        code(joinPoint.getSignature().toShortString())),
                                p(
                                        h4("Duration"),
                                        span(delay))).render())
                .type(SHORT_INBOUND)
                .build());
        lsdContext.capture(deactivation().of(appName.getValue()).build());
    }

    public void captureScheduledError(ProceedingJoinPoint joinPoint, ZonedDateTime startTime, ZonedDateTime endTime, Throwable e) {
        lsdContext.capture(messageBuilder()
                .id(lsdContext.getIdGenerator().next())
                .to(appName.getValue())
                .label("<$clock{scale=0.3}> ")
                .type(SHORT_INBOUND)
                .colour("red")
                .data(p(
                        p(
                                h4("Scheduler Error"),
                                code(joinPoint.getSignature().toShortString())
                        ),
                        p(
                                h4("Exception"),
                                code(e.toString())
                        ),
                        p(
                                h4("Duration"),
                                span(MILLIS.between(startTime, endTime) + "ms")
                        )).render())
                .build());
    }

    private String renderHtmlForMethodCall(Object[] args, Object response) {
        return p(p(
                        h4("Arguments"),
                        code(prettyPrintArgs(args))),
                p(
                        h4("Response"),
                        code(ofNullable(response)
                                .map(PrettyPrinter::prettyPrintJson)
                                .orElse("")
                        ))).render();
    }

    private String renderHtmlForException(String signature, Object[] args, Throwable throwable) {
        return p(
                p(h4("Invoked"), code(signature)),
                p(h4("Arguments"), code(prettyPrintArgs(args))),
                p(h4("Exception"), code(throwable.toString()))
        ).render();
    }

    private String prettyPrintArgs(Object[] args) {
        return stream(args)
                .map(PrettyPrinter::prettyPrintJson)
                .collect(joining(lineSeparator()));
    }
}
