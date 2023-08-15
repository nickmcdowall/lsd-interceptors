package io.lsdconsulting.intercceptors.example.config

import com.fasterxml.jackson.databind.ObjectMapper
import feign.codec.Decoder
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.cloud.openfeign.support.SpringDecoder
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
class AppConfiguration {
    @Bean
    fun feignDecoder(): Decoder {
        return SpringDecoder { HttpMessageConverters() }
    }

    @Bean
    fun converter(): MappingJackson2HttpMessageConverter {
        return MappingJackson2HttpMessageConverter(ObjectMapper())
    }
}
