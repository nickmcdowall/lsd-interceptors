package io.lsdconsulting.interceptors.aop;

import com.lsd.LsdContext;
import com.lsd.diagram.ValidComponentName;
import com.lsd.events.Markup;
import com.lsd.events.ShortMessageInbound;
import io.lsdconsulting.interceptors.common.AppName;
import io.lsdconsulting.interceptors.http.common.PrettyPrinter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

import java.time.ZonedDateTime;

import static com.lsd.events.ArrowType.DOTTED_THIN;
import static io.lsdconsulting.interceptors.http.common.PrettyPrinter.prettyPrint;
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
        var args = joinPoint.getArgs();
        var body = renderHtmlForMethodCall(args, resultValue);
        var signature = joinPoint.getSignature().toShortString();

        lsdContext.capture(icon + " " + signature + " from " + ValidComponentName.of(sourceName) + " to " + ValidComponentName.of(destinationName), body);
    }

    public void captureInternalException(JoinPoint joinPoint, Throwable throwable, String icon) {
        captureException(joinPoint, throwable, appName.getValue(), appName.getValue(), icon);
    }

    public void captureException(JoinPoint joinPoint, Throwable throwable, String sourceName, String destinationName, String icon) {
        var body = renderHtmlForException(joinPoint.getSignature().toShortString(), joinPoint.getArgs(), throwable);
        var exceptionName = throwable.getClass().getSimpleName();
        lsdContext.capture(icon + " " + exceptionName + " response from " + ValidComponentName.of(sourceName) + " to " +  ValidComponentName.of(destinationName) + " [#red]", body);
    }

    public void captureScheduledStart(ProceedingJoinPoint joinPoint, ZonedDateTime startTime) {
        lsdContext.capture(ShortMessageInbound.builder()
                .id(lsdContext.getIdGenerator().next())
                .to(appName.getValue())
                .label("<$clock{scale=0.3}> ")
                .arrowType(DOTTED_THIN)
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
        lsdContext.capture(new Markup("activate " + appName.getValue() + "#skyblue"));
    }

    public void captureScheduledEnd(ProceedingJoinPoint joinPoint, ZonedDateTime startTime, ZonedDateTime endTime) {
        String delay = MILLIS.between(startTime, endTime) + "ms";
        lsdContext.capture(ShortMessageInbound.builder()
                .id(lsdContext.getIdGenerator().next())
                .to(appName.getValue())
                .label("<$clock{scale=0.3}>")
                .data(
                        p(
                                p(
                                        h4("Scheduler completed"),
                                        code(joinPoint.getSignature().toShortString())
                                ),
                                p(
                                        h4("Duration"),
                                        span(delay)
                                )).render())
                .arrowType(DOTTED_THIN)
                .build());
        lsdContext.capture(new Markup("deactivate " + appName.getValue()));
    }

    public void captureScheduledError(ProceedingJoinPoint joinPoint, ZonedDateTime startTime, ZonedDateTime endTime, Throwable e) {
        lsdContext.capture(ShortMessageInbound.builder()
                .id(lsdContext.getIdGenerator().next())
                .to(appName.getValue())
                .label("<$clock{scale=0.3}> ")
                .arrowType(DOTTED_THIN)
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
        return p(
                p(
                        h4("Arguments"),
                        code(prettyPrintArgs(args))
                ),
                p(
                        h4("Response"),
                        code(ofNullable(response)
                                .map(r -> prettyPrint(r.toString()))
                                .orElse("")
                        )
                )
        ).render();
    }

    private String renderHtmlForException(String signature, Object[] args, Throwable throwable) {
        return p(
                p(
                        h4("Invoked"),
                        code(signature)
                ),
                p(
                        h4("Arguments"),
                        code(prettyPrintArgs(args))
                ),
                p(
                        h4("Exception"),
                        code(throwable.toString())
                )
        ).render();
    }

    private String prettyPrintArgs(Object[] args) {
        return stream(args)
                .map(Object::toString)
                .map(PrettyPrinter::prettyPrint)
                .collect(joining(lineSeparator()));
    }
}
