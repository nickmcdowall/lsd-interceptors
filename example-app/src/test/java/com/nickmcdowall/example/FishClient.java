package com.nickmcdowall.example;

import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "fishClient", url = "${server.url}", configuration = FishClient.FishClientConfig.class)
public interface FishClient {

    @PostMapping(value = "/fish")
    void post(NewFishRequest request);

    @GetMapping("/fish/{name}")
    String getFishWithName(@PathVariable String name);

    @DeleteMapping("/fish/{name}")
    void deleteByName(@PathVariable String name);

    @PostMapping(value = "/fish/{id}/{name}")
    void createFish(@PathVariable long id, @PathVariable String name);

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
