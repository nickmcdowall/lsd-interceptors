package com.nickmcdowall.example;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import feign.codec.Decoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@EnableFeignClients(clients = FishClient.class)
@Configuration
public class TestConfig {

    @Bean
    public TestState testState() {
        return new TestState();
    }
}
