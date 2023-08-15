package io.lsdconsulting.intercceptors.example

import feign.RequestInterceptor
import feign.RequestTemplate
import io.lsdconsulting.intercceptors.example.FishClient.FishClientConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.*

@FeignClient(name = "fishClient", url = "\${server.url}", configuration = [FishClientConfig::class])
interface FishClient {
    @PostMapping(value = ["/fish"])
    fun post(request: NewFishRequest?)

    @GetMapping("/fish/{name}")
    fun getFishWithName(@PathVariable name: String?): String?

    @GetMapping("/fish")
    fun getFishByName(@RequestParam("name") name: String?): List<String?>?

    @DeleteMapping("/fish/{name}")
    fun deleteByName(@PathVariable name: String?)

    @PostMapping(value = ["/fish/{id}/{name}"])
    fun createFish(@PathVariable id: Long, @PathVariable name: String?)
    class FishClientConfig {
        @Bean
        fun requestInterceptor(): RequestInterceptor {
            return RequestInterceptor { template: RequestTemplate ->
                template.header("Source-Name", "User")
                template.header("Target-Name", "FishApp")
            }
        }
    }
}
