package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.nickmcdowall.lsd.interceptor.common.DestinationNamesMapper;
import com.nickmcdowall.lsd.interceptor.common.RegexResolvingDestinationNameMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * We need a way to name the different endpoints that the RestTemplate hits.
 *
 * By default we can wire in a bean that uses regex to determine what to call the endpoint.
 */
@Configuration
public class LsdDestinationNameMappingConfiguration {

    /**
     * Users can override this by supplying their own DestinationNamesMapper bean called
     * 'restTemplateDestinationMappings` if they prefer to use an alternative strategy.
     */
    @Bean
    @ConditionalOnMissingBean(name = "restTemplateDestinationMappings")
    public DestinationNamesMapper restTemplateDestinationMappings() {
        return new RegexResolvingDestinationNameMapper();
    }

}
