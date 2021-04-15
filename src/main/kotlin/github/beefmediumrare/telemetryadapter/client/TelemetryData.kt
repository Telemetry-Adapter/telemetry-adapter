package github.beefmediumrare.telemetryadapter.client

import java.time.Instant

data class TelemetryData(
    val createdAt: Instant,
    val stateOfCharge: Double,
    val chargingMode: ChargingMode?,
    val parked: Boolean,
    val speed: Double?,
    val externalTemperature: Double?,
    val estimatedRange: Double?,
) {
    enum class ChargingMode {
        DC, AC
    }
}
