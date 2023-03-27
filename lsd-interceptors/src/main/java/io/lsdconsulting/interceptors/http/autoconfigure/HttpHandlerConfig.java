package io.lsdconsulting.interceptors.http.autoconfigure;

import com.lsd.core.LsdContext;
import io.lsdconsulting.interceptors.http.common.DefaultHttpInteractionHandler;
import io.lsdconsulting.interceptors.http.common.HttpInteractionHandler;
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings;
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

@RequiredArgsConstructor
class HttpHandlerConfig {
    private final LsdContext lsdContext = LsdContext.getInstance();
    
    private final SourceNameMappings defaultSourceNameMapping;
    private final DestinationNameMappings defaultDestinationNameMapping;

    @Bean
    @ConditionalOnMissingBean(name = "httpInteractionHandlers")
    public List<HttpInteractionHandler> httpInteractionHandlers() {
        return List.of(new DefaultHttpInteractionHandler(lsdContext, defaultSourceNameMapping, defaultDestinationNameMapping));
    }
}
