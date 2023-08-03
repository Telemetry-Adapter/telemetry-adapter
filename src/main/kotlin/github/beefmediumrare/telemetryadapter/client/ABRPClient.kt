package github.beefmediumrare.telemetryadapter.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Headers
import feign.Param
import feign.RequestLine

interface ABRPClient {

    @RequestLine("POST /tlm/send?token={token}&tlm={telemetryParameters}")
    @Headers(
        "Authorization: APIKEY {apiKey}",
        "Content-Type: application/x-www-form-urlencoded",
    )
    fun postTelemetryData(
        @Param("token")
        userToken: String,
        @Param
        telemetryParameters: TelemetryParameters,
        @Param("apiKey")
        apiKey: String,
    ): PostTelemetryDataResponse

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
        @JsonProperty("speed")
        val speed: Double?,
        @JsonProperty("is_parked")
        val parked: Boolean,
        // Outside temperature measured by the vehicle in °C
        @JsonProperty("ext_temp")
        val externalTemperature: Double?,
    ) {
        override fun toString(): String =
            jacksonObjectMapper().writeValueAsString(this)
    }

    data class PostTelemetryDataResponse(
        @JsonProperty("status")
        val status: String,
        @JsonProperty("new_tlm")
        val isNew: Boolean,
        @JsonProperty("missing")
        val missingFields: String,
    )
}
