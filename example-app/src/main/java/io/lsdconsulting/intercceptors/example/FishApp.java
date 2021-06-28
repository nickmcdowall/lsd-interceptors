package io.lsdconsulting.intercceptors.example;

import io.lsdconsulting.intercceptors.example.config.AppConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(AppConfiguration.class)
@SpringBootApplication
public class FishApp {

    public static void main(String[] args) {
        SpringApplication.run(FishApp.class, args);
    }
}
