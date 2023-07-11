package io.lsdconsulting.interceptors.messaging.autoconfigure;

import com.lsd.core.LsdContext;
import io.lsdconsulting.interceptors.messaging.ErrorPublisherInterceptor;
import io.lsdconsulting.interceptors.messaging.EventConsumerInterceptor;
import io.lsdconsulting.interceptors.messaging.EventPublisherInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.messaging.support.ChannelInterceptor;

/**
 * <p>
 * If a {@link LsdContext} and ChannelInterceptor classes is available it will automatically autoconfig a {@link LsdMessagingConfiguration}
 * </p>
 */
@Configuration
@PropertySource(ignoreResourceNotFound = true, value = "classpath:lsd.properties")
@ConditionalOnClass(value = {LsdContext.class, ChannelInterceptor.class})
@ConditionalOnProperty(name = "lsd.interceptors.autoconfig.enabled", havingValue = "true", matchIfMissing = true)
public class LsdMessagingConfiguration {

    @Value("${info.app.name}")
    private String appName;

    private final LsdContext lsdContext = LsdContext.getInstance();

    @Bean
    @GlobalChannelInterceptor(patterns = "*-in-*", order = 100)
    public EventConsumerInterceptor eventConsumerInterceptor() {
        return new EventConsumerInterceptor(lsdContext);
    }

    @Bean
    @GlobalChannelInterceptor(patterns = "*-out-*", order = 101)
    public EventPublisherInterceptor eventPublisherInterceptor() {
        return new EventPublisherInterceptor(lsdContext);
    }

    @Bean
    @GlobalChannelInterceptor(patterns = "*errorChannel", order = 101)
    public ErrorPublisherInterceptor errorPublisherInterceptor() {
        return new ErrorPublisherInterceptor(lsdContext, appName);
    }
}
