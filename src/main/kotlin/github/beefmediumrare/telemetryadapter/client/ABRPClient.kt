package github.beefmediumrare.telemetryadapter.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.net.URLEncoder
import java.net.http.HttpHeaders
import java.nio.charset.StandardCharsets
import java.time.Instant

@Component
class ABRPClient(
    @Value("\${abrp.authToken}")
    private val authToken: String,
    @Value("\${abrp.userToken}")
    private val userToken: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val client = WebClient.builder()
        .baseUrl("https://api.iternio.com/1")
        .defaultHeaders {
            it.set("Authorization", "APIKEY $authToken")
        }
        .build()

    fun postTelemetryData(telemetryData: TelemetryData) {
        logger.info("postTelemetryData called with $telemetryData")
        data class TelemetryParameters(
            // Current UTC timestamp (epoch) in seconds (note, not milliseconds!)
            @JsonProperty("utc")
            val timestamp: Long,
            // State of Charge in %
            @JsonProperty("soc")
            val stateOfCharge: Double?,
            @JsonProperty("est_battery_range")
            // Estimated remaining range of the vehicle in km
            val estimatedRange: Double?,
            @JsonProperty("is_charging")
            val charging: Boolean,
            // If is_charging, indicate if this is DC fast charging
            @JsonProperty("is_dcfc")
            val fastCharging: Boolean,
            val speed: Double?,
            @JsonProperty("is_parked")
            val parked: Boolean,
            // Outside temperature measured by the vehicle in °C
            @JsonProperty("ext_temp")
            val externalTemperature: Double?,
        )

        data class Response(
            val status: String,
            @JsonProperty("new_tlm")
            val isNew: Boolean,
            @JsonProperty("missing")
            val missingFields: String,
        )

        val telemetryParameters = with(telemetryData) {
            TelemetryParameters(
                timestamp = createdAt.epochSecond,
                stateOfCharge = stateOfCharge,
                charging = chargingMode != null,
                fastCharging = chargingMode == TelemetryData.ChargingMode.DC,
                parked = parked,
                externalTemperature = externalTemperature,
                estimatedRange = estimatedRange,
                speed = speed,
            )
        }.let {
            jacksonObjectMapper().writeValueAsString(it)
        }

        logger.info("posting…")
        val responseBody = client
            .post()
            .uri("/tlm/send")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("token", userToken)
                    .with("tlm", telemetryParameters)
            )
            .retrieve()
            .toEntity(Response::class.java)
            .block()
            .let {
                checkNotNull(it?.body) { "Unexpected empty response" }
            }

        logger.info("Got response $responseBody")
    }
}
