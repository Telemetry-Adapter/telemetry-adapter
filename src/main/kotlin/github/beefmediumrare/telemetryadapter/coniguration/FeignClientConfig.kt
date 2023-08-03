package github.beefmediumrare.telemetryadapter.coniguration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import feign.Feign
import feign.jackson.JacksonDecoder
import github.beefmediumrare.telemetryadapter.client.ABRPClient
import github.beefmediumrare.telemetryadapter.client.PSAAuthClient
import github.beefmediumrare.telemetryadapter.client.PSAClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignClientConfig {
    @Bean
    fun jacksonDecoder(): JacksonDecoder = JacksonDecoder(
        jacksonMapperBuilder()
            .addModules(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build()
    )

    @Bean
    fun abrpFeignClient(
        jacksonDecoder: JacksonDecoder,
    ): ABRPClient = Feign.builder()
        .decoder(jacksonDecoder)
        .target(
            ABRPClient::class.java,
            "https://api.iternio.com/1",
        )

    @Bean
    fun psaFeignClient(
        jacksonDecoder: JacksonDecoder,
    ): PSAClient = Feign.builder()
        .decoder(jacksonDecoder)
        .target(
            PSAClient::class.java,
            "https://api.groupe-psa.com/connectedcar/v4/user",
        )

    @Bean
    fun psaAuthClient(
        jacksonDecoder: JacksonDecoder,
    ): PSAAuthClient = Feign.builder()
        .decoder(jacksonDecoder)
        .target(
            PSAAuthClient::class.java,
            "https://idpcvs.peugeot.com/am",
        )
}
