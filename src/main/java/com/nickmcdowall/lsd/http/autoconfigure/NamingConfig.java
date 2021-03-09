package com.nickmcdowall.lsd.http.autoconfigure;

import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.RegexResolvingNameMapper;
import com.nickmcdowall.lsd.http.naming.AppName;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

class NamingConfig {
    @Value("${info.app.name:App}")
    private String appName;

    @Bean
    @ConditionalOnMissingBean(name = "defaultSourceNameMapping")
    public SourceNameMappings defaultSourceNameMapping() {
        return new AppName(appName);
    }

    @Bean
    @ConditionalOnMissingBean(name = "defaultDestinationNameMapping")
    public DestinationNameMappings defaultDestinationNameMapping() {
        return new RegexResolvingNameMapper();
    }
}
