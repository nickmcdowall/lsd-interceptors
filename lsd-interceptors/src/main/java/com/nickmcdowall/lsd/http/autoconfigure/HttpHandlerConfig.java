package com.nickmcdowall.lsd.http.autoconfigure;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.common.DefaultHttpInteractionHandler;
import com.nickmcdowall.lsd.http.common.HttpInteractionHandler;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

@RequiredArgsConstructor
class HttpHandlerConfig {
    private final TestState testState;
    private final SourceNameMappings defaultSourceNameMapping;
    private final DestinationNameMappings defaultDestinationNameMapping;

    @Bean
    @ConditionalOnMissingBean(name = "httpInteractionHandlers")
    public List<HttpInteractionHandler> httpInteractionHandlers() {
        return List.of(new DefaultHttpInteractionHandler(testState, defaultSourceNameMapping, defaultDestinationNameMapping));
    }
}
