package com.nickmcdowall.lsd.interceptor.autoconfigure;

import com.nickmcdowall.lsd.interceptor.common.DestinationNamesMapper;
import com.nickmcdowall.lsd.interceptor.common.RegexResolvingDestinationNameMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * We need a way to name the different endpoints that the RestTemplate hits.
 * <p>
 * By default we can wire in a bean that uses regex to determine what to call the endpoint.
 */
@Configuration
public class LsdDestinationNameMappingConfiguration {

    public static final DestinationNamesMapper APP_ONLY_DESTINATION = path -> "App";

    /**
     * Users can override this by supplying their own DestinationNamesMapper bean called
     * 'defaultAppToDestinationNameMappings` if they prefer to use an alternative strategy.
     */
    @Bean
    @ConditionalOnMissingBean(name = "defaultAppToDestinationNameMappings")
    public DestinationNamesMapper defaultAppToDestinationNameMappings() {
        return new RegexResolvingDestinationNameMapper();
    }

    /**
     * Users can override this by supplying their own DestinationNamesMapper bean called
     * 'defaultUserToAppNameMapping` if they prefer to use an alternative strategy.
     *
     * Assumes the test rest template is for invoking the application as if it was a user hence the destination name
     * defaults to 'App'.
     */
    @Bean
    @ConditionalOnMissingBean(name = "defaultUserToAppNameMapping")
    public DestinationNamesMapper defaultUserToAppNameMapping() {
        return APP_ONLY_DESTINATION;
    }

}
