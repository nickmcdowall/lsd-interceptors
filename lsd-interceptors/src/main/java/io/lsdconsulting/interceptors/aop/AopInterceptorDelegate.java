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
import static java.time.Duration.ofMillis;
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
                        .data(div(
                                section(
                                        h3("Scheduled"),
                                        span(joinPoint.getSignature().toShortString())
                                ),
                                section(
                                        h3("Timestamp"),
                                        span(startTime.format(ISO_DATE_TIME))
                                )).render())
                        .build(),
                activation().of(appName.getValue()).colour("skyblue").build()
        );
    }

    public void captureScheduledEnd(ProceedingJoinPoint joinPoint, ZonedDateTime startTime, ZonedDateTime endTime) {
        long duration = MILLIS.between(startTime, endTime);
        lsdContext.capture(
                messageBuilder()
                        .id(lsdContext.getIdGenerator().next())
                        .to(appName.getValue())
                        .label("<$clock{scale=0.3}>")
                        .data(
                                div(
                                        section(
                                                h3("Scheduler completed"),
                                                span(joinPoint.getSignature().toShortString())),
                                        section(
                                                h3("Duration"),
                                                span(duration + "ms"))
                                ).render())
                        .type(SHORT_INBOUND)
                        .duration(ofMillis(duration))
                        .build(),
                deactivation().of(appName.getValue()).build()
        );
    }

    public void captureScheduledError(ProceedingJoinPoint joinPoint, ZonedDateTime startTime, ZonedDateTime endTime, Throwable e) {
        var duration = MILLIS.between(startTime, endTime);
        lsdContext.capture(messageBuilder()
                .id(lsdContext.getIdGenerator().next())
                .to(appName.getValue())
                .label("<$clock{scale=0.3}> ")
                .type(SHORT_INBOUND)
                .colour("red")
                .data(div(
                        section(
                                h3("Scheduler Error"),
                                span(joinPoint.getSignature().toShortString())
                        ),
                        section(
                                h3("Exception"),
                                span(e.toString())
                        ),
                        section(
                                h3("Duration"),
                                span(duration + "ms")
                        )).render())
                .duration(ofMillis(duration))
                .build());
    }

    private String renderHtmlForMethodCall(Object[] args, Object response) {
        return div(section(
                        h3("Arguments"),
                        span(prettyPrintArgs(args))),
                section(
                        h3("Response"),
                        p(ofNullable(response)
                                .map(PrettyPrinter::prettyPrintJson)
                                .orElse("")
                        ))).render();
    }

    private String renderHtmlForException(String signature, Object[] args, Throwable throwable) {
        return div(
                section(h3("Invoked"), span(signature)),
                section(h3("Arguments"), span(prettyPrintArgs(args))),
                section(h3("Exception"), p(throwable.toString()))
        ).render();
    }

    private String prettyPrintArgs(Object[] args) {
        return stream(args)
                .map(PrettyPrinter::prettyPrintJson)
                .collect(joining(lineSeparator()));
    }
}
