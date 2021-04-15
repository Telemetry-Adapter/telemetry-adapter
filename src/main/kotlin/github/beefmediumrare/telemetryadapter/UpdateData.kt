package github.beefmediumrare.telemetryadapter

import github.beefmediumrare.telemetryadapter.client.ABRPClient
import github.beefmediumrare.telemetryadapter.client.PSAClient
import github.beefmediumrare.telemetryadapter.client.TelemetryData
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UpdateData(
    private val environment: Environment,
    private val psaClient: PSAClient,
    private val abrpClient: ABRPClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("#".repeat(5) + " 1.0.0 " + "#".repeat(5))
    }

    // PSA API Rate limit: 100 Requests / 1h
    @Scheduled(fixedRate = 36_000)
    operator fun invoke() {
        logger.info("Invoked")
        abrpClient.postTelemetryData(psaClient.getTelemetryData())
    }
}
