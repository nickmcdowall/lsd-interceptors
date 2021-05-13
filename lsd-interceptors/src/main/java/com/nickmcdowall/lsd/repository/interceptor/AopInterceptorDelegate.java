package com.nickmcdowall.lsd.repository.interceptor;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.common.PrettyPrinter;
import com.nickmcdowall.lsd.http.naming.AppName;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;

import static com.nickmcdowall.lsd.http.common.PrettyPrinter.prettyPrint;
import static j2html.TagCreator.*;
import static java.lang.System.lineSeparator;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

@Slf4j
@RequiredArgsConstructor
public class AopInterceptorDelegate {
    private final TestState testState;
    private final AppName appName;

    public void captureInternalInteraction(JoinPoint joinPoint, Object resultValue, String icon) {
        captureInteraction(joinPoint, resultValue, appName.getValue(), appName.getValue(), icon);
    }

    public void captureInteraction(JoinPoint joinPoint, Object resultValue, String sourceName, String destinationName, String icon) {
        var args = joinPoint.getArgs();
        var body = renderHtmlForMethodCall(args, resultValue);
        var signature = joinPoint.getSignature().toShortString();

        testState.log(icon + " " + signature + " from " + sourceName + " to " + destinationName, body);
    }

    public void captureInternalException(JoinPoint joinPoint, Throwable throwable, String icon) {
        captureException(joinPoint, throwable, appName.getValue(), appName.getValue(), icon);
    }

    public void captureException(JoinPoint joinPoint, Throwable throwable, String sourceName, String destinationName, String icon) {
        var body = renderHtmlForException(joinPoint.getSignature().toShortString(), joinPoint.getArgs(), throwable);
        var exceptionName = throwable.getClass().getSimpleName();
        testState.log(icon + " " + exceptionName + " response from " + sourceName + " to " + destinationName + " [#red]", body);
    }

    private String renderHtmlForMethodCall(Object[] args, Object response) {
        return p(
                p(
                        h4("Arguments:"),
                        sub(prettyPrintArgs(args))
                ),
                p(
                        h4("Response:"),
                        pre(ofNullable(response)
                                .map(r -> prettyPrint(r.toString()))
                                .orElse("")
                        )
                )
        ).render();
    }

    private String renderHtmlForException(String signature, Object[] args, Throwable throwable) {
        return p(
                p(
                        h4("Invoked:"),
                        sub(signature)
                ),
                p(
                        h4("Arguments:"),
                        sub(prettyPrintArgs(args))
                ),
                p(
                        h4("Exception:"),
                        pre(throwable.toString())
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
