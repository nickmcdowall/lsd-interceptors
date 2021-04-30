package com.nickmcdowall.lsd.http.interceptor;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.common.PrettyPrinter;
import com.nickmcdowall.lsd.http.naming.AppName;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;

import static j2html.TagCreator.*;
import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public
class AopInterceptorDelegate {
    private final TestState testState;
    private final AppName appName;

    public void logInternalResponse(Object resultValue, JoinPoint joinPoint) {
        var methodName = joinPoint.getSignature().getName();
        var args = joinPoint.getArgs();
        var className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        var body = renderHtmlForMethodCall(className, methodName, args, resultValue);
        testState.log(methodName + "( " + joinArgumentTypeNames(args) + " ) from " + appName.getValue() + " to " + appName.getValue(), body);
    }

    /**
     * Used to show an internal (within the application) exception (e.g. when calling DB)
     */
    public void logInternalException(@NonNull Throwable throwable) {
        testState.log(throwable.getClass().getSimpleName() + " response from " + appName.getValue() + " to " + appName.getValue() + " [#red]", throwable);
    }

    private String renderHtmlForMethodCall(String className, String methodName, Object[] args, Object response) {
        var popupValue =
                p(
                        p(
                                h4("Invoked:"),
                                sub(className + "." + methodName)
                        ),
                        p(
                                h4("Arguments:"),
                                sub(prettyPrintArgs(args))
                        ),
                        p(
                                h4("Response:"),
                                pre(PrettyPrinter.prettyPrint(response.toString()))
                        )
                ).render();
        return popupValue;
    }

    private String prettyPrintArgs(Object[] args) {
        return stream(args)
                .map(Object::toString)
                .map(PrettyPrinter::prettyPrint)
                .collect(joining(lineSeparator()));
    }

    private String joinArgumentTypeNames(Object[] args) {
        return join(",", stream(args)
                .map(arg -> arg.getClass().getSimpleName())
                .collect(toList()));
    }
}
