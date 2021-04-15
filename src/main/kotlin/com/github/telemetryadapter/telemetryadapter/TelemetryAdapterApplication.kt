package com.github.telemetryadapter.telemetryadapter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TelemetryAdapterApplication

fun main(args: Array<String>) {
    runApplication<TelemetryAdapterApplication>(*args)
}
