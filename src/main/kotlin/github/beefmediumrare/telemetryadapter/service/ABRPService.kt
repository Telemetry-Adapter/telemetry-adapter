package github.beefmediumrare.telemetryadapter.service

import github.beefmediumrare.telemetryadapter.client.ABRPClient
import github.beefmediumrare.telemetryadapter.model.TelemetryData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ABRPService(
    @Value("\${abrp.apiKey}")
    private val apiKey: String,
    @Value("\${abrp.userToken}")
    private val userToken: String,
    private val abrpClient: ABRPClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun postTelemetryData(telemetryData: TelemetryData) {
        logger.info("postTelemetryData called with $telemetryData")

        val telemetryParameters = with(telemetryData) {
            ABRPClient.TelemetryParameters(
                timestamp = createdAt.epochSecond,
                stateOfCharge = stateOfCharge,
                charging = chargingMode != null,
                fastCharging = chargingMode == TelemetryData.ChargingMode.DC,
                parked = parked,
                externalTemperature = externalTemperature,
                estimatedRange = estimatedRange,
                speed = speed,
            )
        }

        logger.info("posting…")
        val responseBody = abrpClient.postTelemetryData(
            userToken = userToken,
            telemetryParameters = telemetryParameters,
            apiKey = apiKey,
        )

        logger.info("Got response $responseBody")
    }
}
