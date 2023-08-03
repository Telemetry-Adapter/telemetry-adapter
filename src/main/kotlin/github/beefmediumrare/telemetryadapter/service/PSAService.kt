package github.beefmediumrare.telemetryadapter.service

import github.beefmediumrare.telemetryadapter.client.PSAClient
import github.beefmediumrare.telemetryadapter.model.TelemetryData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PSAService(
    @Value("\${psa.clientId}")
    private val clientId: String,
    @Value("\${psa.user.vin}")
    private val vehicleID: String,

    private val authClient: PSAAuthService,
    private val psaClient: PSAClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val vehicleId: String by lazy {
        logger.info("getVehicleId called")

        val responseBody = authClient.withAuthentication { authToken ->
            psaClient.getVehicles(
                clientId = clientId,
                authToken = authToken,
            )
        }
        logger.info("Got response $responseBody")

        responseBody.embedded.vehicles.single { it.vin == vehicleID }.id
    }

    fun getTelemetryData(): TelemetryData {
        logger.info("getTelemetryData called")

        val responseBody = authClient.withAuthentication { authToken ->
            psaClient.getVehicleStatus(
                vehicleId = vehicleId,
                clientId = clientId,
                authToken = authToken,
            )
        }
        logger.info("Got response $responseBody")

        return with(responseBody) {
            val energy = energy.single { it.type == "Electric" }

            TelemetryData(
                createdAt = energy.createdAt,
                stateOfCharge = energy.level,
                chargingMode = when (energy.charging!!.chargingMode) {
                    "No" -> null
                    "Slow" -> TelemetryData.ChargingMode.AC
                    "Quick" -> TelemetryData.ChargingMode.DC
                    else -> throw IllegalArgumentException("Unknown chargingMode '${energy.charging.chargingMode}'")
                },
                parked = !kinetic.moving,
                externalTemperature = environment.air.temperature,
                estimatedRange = energy.autonomy,
                speed = kinetic.speed,
            )
        }
    }
}
