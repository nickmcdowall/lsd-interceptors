package io.lsdconsulting.intercceptors.example

import io.lsdconsulting.intercceptors.example.config.AppConfiguration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

@Import(AppConfiguration::class)
@SpringBootApplication
open class FishApp

fun main(args: Array<String>) {
    SpringApplication.run(FishApp::class.java, *args)
}
