package github.beefmediumrare.telemetryadapter.client

import com.fasterxml.jackson.annotation.JsonProperty
import feign.Headers
import feign.Param
import feign.RequestLine
import java.time.Instant

interface PSAClient {
    @RequestLine("GET /vehicles?client_id={clientId}")
    @Headers(
        "Authorization: Bearer {authToken}",
        "x-introspect-realm: clientsB2CPeugeot",
        "Accept: application/hal+json",
    )
    fun getVehicles(
        @Param("clientId")
        clientId: String,
        @Param("authToken")
        authToken: String,
    ): GetVehiclesResponse

    @RequestLine("GET /vehicles/{vehicleId}/status?client_id={clientId}&extension=kinetic")
    @Headers(
        "Authorization: Bearer {authToken}",
        "x-introspect-realm: clientsB2CPeugeot",
        "Accept: application/hal+json",
    )
    fun getVehicleStatus(
        @Param("vehicleId")
        vehicleId: String,
        @Param("clientId")
        clientId: String,
        @Param("authToken")
        authToken: String,
    ): GetVehicleStatusResponse

    data class GetVehiclesResponse(
        @JsonProperty("_embedded")
        val embedded: EmbeddedData,
    ) {

        data class EmbeddedData(
            @JsonProperty("vehicles")
            val vehicles: List<Vehicle>,
        ) {

            data class Vehicle(
                @JsonProperty("id")
                val id: String,
                @JsonProperty("vin")
                val vin: String,
            )
        }
    }

    data class GetVehicleStatusResponse(
        @JsonProperty("energy")
        val energy: List<Energy>,
        @JsonProperty("kinetic")
        val kinetic: Kinetic,
        @JsonProperty("environment")
        val environment: Environment,
    ) {
        data class Energy(
            // "Fuel" "Electric"
            @JsonProperty("type")
            val type: String,
            // [0…100]
            @JsonProperty("level")
            val level: Double,
            // Vehicle autonomy for this energy class expressed in KM
            @JsonProperty("autonomy")
            val autonomy: Double?,
            @JsonProperty("charging")
            val charging: Charging?,
            @JsonProperty("createdAt")
            val createdAt: Instant,
        ) {
            data class Charging(
                // "No" "Slow" "Quick"
                @JsonProperty("chargingMode")
                val chargingMode: String,
                // [0…500] km/h
                @JsonProperty("chargingRate")
                val chargingRate: Int,
            )
        }

        data class Kinetic(
            @JsonProperty("moving")
            val moving: Boolean,
            @JsonProperty("speed")
            val speed: Double?,
            @JsonProperty("acceleration")
            val acceleration: Double?,
        )

        data class Environment(
            @JsonProperty("air")
            val air: Air,
        ) {
            data class Air(
                @JsonProperty("temp")
                val temperature: Double?,
            )
        }
    }
}
