package io.lsdconsulting.intercceptors.example

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@EnableFeignClients(clients = [FishClient::class])
@Configuration
open class TestConfig
