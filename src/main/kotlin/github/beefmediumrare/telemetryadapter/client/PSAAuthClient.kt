package github.beefmediumrare.telemetryadapter.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import kotlin.math.log

@Component
class PSAAuthClient(
    @Value("\${psa.clientId}")
    private val clientId: String,
    @Value("\${psa.oauth.password}")
    private val oAuthPassword: String,
    @Value("\${psa.user.name}")
    private val username: String,
    @Value("\${psa.user.password}")
    private val userPassword: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val authClient = WebClient.builder()
        .baseUrl("https://idpcvs.peugeot.com/am/oauth2/access_token")
        .defaultHeaders {
            it.setBasicAuth(clientId, oAuthPassword)
        }
        .build()

    private var authToken: String = ""

    private fun authenticate(): String {
        logger.info("authenticate called")
        data class Response(
            @JsonProperty("access_token")
            val accessToken: String,
        )

        logger.info("posting…")
        val responseBody = authClient
            .post()
            .headers {
                it.contentType = MediaType.APPLICATION_FORM_URLENCODED
            }
            .body(
                BodyInserters
                    .fromFormData("realm", "clientsB2CPeugeot")
                    .with("grant_type", "password")
                    .with("username", username)
                    .with("password", userPassword)
                    .with("scope", "profile openid")
            )
            .retrieve()
            .toEntity(Response::class.java)
            .block()
            .let {
                checkNotNull(it?.body) { "Unexpected empty response" }
            }

        logger.info("success")
        return responseBody.accessToken
    }

    fun <T> withAuthentication(fn: (String) -> T) = try {
        fn(authToken)
    } catch (e: WebClientResponseException.Unauthorized) {
        logger.info("Unauthorized")
        authToken = authenticate()
        fn(authToken)
    }
}
