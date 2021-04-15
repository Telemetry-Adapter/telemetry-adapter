package github.beefmediumrare.telemetryadapter.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.time.Instant

@Component
class PSAClient(
    @Value("\${psa.clientId}")
    private val clientId: String,
    @Value("\${psa.user.vin}")
    private val vehicleID: String,

    private val authClient: PSAAuthClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val apiClient = WebClient.builder()
        .baseUrl("https://api.groupe-psa.com/connectedcar/v4/user")
        .defaultHeaders {
            it.set("x-introspect-realm", "clientsB2CPeugeot")
            it.set("Accept", "application/hal+json")
        }

        .build()

    private lateinit var vehicleId: String

    init {
        vehicleId = getVehicleId()
    }

    private fun getVehicleId(): String {
        logger.info("getVehicleId called")
        data class Vehicle(
            val id: String,
            val vin: String,
        )

        data class Embedded(
            val vehicles: List<Vehicle>,
        )

        data class Response(
            @JsonProperty("_embedded")
            val embedded: Embedded
        )

        logger.info("getting…")
        val responseBody = authClient.withAuthentication { authToken ->
            apiClient
                .get()
                .uri {
                    it.path("/vehicles")
                        .queryParam("client_id", clientId)
                        .build()
                }
                .headers {
                    it.setBearerAuth(authToken)
                }
                .retrieve()
                .toEntity(Response::class.java)
                .timeout(Duration.ofSeconds(30))
                .block()
                .let {
                    checkNotNull(it?.body) { "Unexpected empty response" }
                }
        }
        logger.info("Got response $responseBody")

        return responseBody.embedded.vehicles.single{ it.vin == vehicleID }.id
    }

    fun getTelemetryData(): TelemetryData {
        logger.info("getTelemetryData called")
        data class Charging(
            // "No" "Slow" "Quick"
            val chargingMode: String,
            // [0…500] km/h
            val chargingRate: Int,
        )

        data class Energy(
            // "Fuel" "Electric"
            val type: String,
            // [0…100]
            val level: Double,
            // Vehicle autonomy for this energy class expressed in KM
            val autonomy: Double?,
            val charging: Charging?,
            val createdAt: Instant,
        )

        data class Kinetic(
            val moving: Boolean,
            val speed: Double?,
            val acceleration: Double?,
        )

        data class Air(
            @JsonProperty("temp")
            val temperature: Double?,
        )

        data class Environment(
            val air: Air,
        )

        data class Response(
            val energy: List<Energy>,
            val kinetic: Kinetic,
            val environment: Environment,
        )

        logger.info("getting…")
        val responseBody = authClient.withAuthentication { authToken ->
            apiClient
                .get()
                .uri {
                    it.path("/vehicles/{vehicle_id}/status")
                        .queryParam("client_id", clientId)
                        .queryParam("extension", "kinetic")
                        .build(vehicleId)
                }
                .headers {
                    it.setBearerAuth(authToken)
                }
                .retrieve()
                .toEntity(Response::class.java)
                .onErrorComplete()
                .block()
                .let {
                    checkNotNull(it?.body) { "Unexpected empty response" }
                }
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
