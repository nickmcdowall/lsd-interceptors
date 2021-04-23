package com.nickmcdowall.example;

import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "fishClient", url = "${server.url}", configuration = FishClient.FishClientConfig.class)
public interface FishClient {

    @PostMapping(value = "/fish")
    void post(NewFishRequest request);

    class FishClientConfig {
        @Bean
        public RequestInterceptor requestInterceptor() {
            return template -> {
                template.header("Source-Name", "User");
                template.header("Target-Name", "FishApp");
            };
        }
    }
}
