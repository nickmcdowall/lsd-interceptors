package com.nickmcdowall.example;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;


@EnableFeignClients(clients = FishClient.class)
@Configuration
public class TestConfig {

}
