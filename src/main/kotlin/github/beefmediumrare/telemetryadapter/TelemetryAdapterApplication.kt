 package github.beefmediumrare.telemetryadapter

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TelemetryAdapterApplication

fun main(args: Array<String>) {
    runApplication<TelemetryAdapterApplication>(*args)
}
