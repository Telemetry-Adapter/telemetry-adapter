package github.beefmediumrare.telemetryadapter.usecase

import github.beefmediumrare.telemetryadapter.service.ABRPService
import github.beefmediumrare.telemetryadapter.service.PSAService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UpdateData(
    private val psaService: PSAService,
    private val abrpService: ABRPService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("#".repeat(10) + " 1.0.1 " + "#".repeat(10))
    }

    // PSA API Rate limit: 100 Requests / 1h
    @Scheduled(fixedRate = 36_000)
    operator fun invoke() {
        logger.info("Invoked")

        abrpService.postTelemetryData(psaService.getTelemetryData())
    }
}
