package com.nickmcdowall.lsd.http.interceptor;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public
class AopInterceptorDelegate {
    private final TestState testState;
    private final String appName;

    public void logInternalResponse(Object resultValue, String methodName, Object[] args) {
        testState.log(methodName + "( " + joinArgumentTypeNames(args) + " ) from " + appName + " to " + appName, resultValue);
    }

    /**
     * Used to show an internal (within the application) exception (e.g. when calling DB)
     */
    public void logInternalException(@NonNull Throwable throwable) {
        testState.log(throwable.getClass().getSimpleName() + " response from " + appName + " to " + appName + " [#red]", throwable);
    }

    private String joinArgumentTypeNames(Object[] args) {
        return join(",", stream(args)
                .map(arg -> arg.getClass().getSimpleName())
                .collect(toList()));
    }
}
