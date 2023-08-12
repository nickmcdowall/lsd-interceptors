package io.lsdconsulting.interceptors.http.autoconfigure;

import io.lsdconsulting.interceptors.common.AppName;
import io.lsdconsulting.interceptors.http.naming.AlwaysAppName;
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings;
import io.lsdconsulting.interceptors.http.naming.RegexResolvingNameMapper;
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

class NamingConfig {
    @Value("${info.app.name:App}")
    private String appName;

    @Bean
    @ConditionalOnMissingBean(name = "defaultSourceNameMapping")
    public SourceNameMappings defaultSourceNameMapping() {
        return new AlwaysAppName(AppName.Factory.create(appName));
    }

    @Bean
    @ConditionalOnMissingBean(name = "defaultDestinationNameMapping")
    public DestinationNameMappings defaultDestinationNameMapping() {
        return new RegexResolvingNameMapper();
    }
}
