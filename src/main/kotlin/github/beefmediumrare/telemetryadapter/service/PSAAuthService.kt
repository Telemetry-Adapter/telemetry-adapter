package github.beefmediumrare.telemetryadapter.service

import feign.FeignException
import github.beefmediumrare.telemetryadapter.client.PSAAuthClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Component
class PSAAuthService(
    @Value("\${psa.clientId}")
    private val clientId: String,
    @Value("\${psa.oauth.password}")
    private val oAuthPassword: String,
    @Value("\${psa.user.name}")
    private val username: String,
    @Value("\${psa.user.password}")
    private val userPassword: String,
    private val psaAuthClient: PSAAuthClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private var authToken: String = ""

    @OptIn(ExperimentalEncodingApi::class)
    fun authenticate(): String {
        logger.info("authenticate called")

        val responseBody = psaAuthClient.authenticate(
            realm = "clientsB2CPeugeot",
            grantType = "password",
            username = username,
            password = userPassword,
            scope = "profile openid",
            basicAuthToken = Base64.encode("$clientId:$oAuthPassword".toByteArray())
        )
        logger.info("success")

        return responseBody.accessToken
    }

    fun <T> withAuthentication(fn: (String) -> T) = try {
        fn(authToken)
    } catch (e: FeignException.Unauthorized) {
        logger.info("Unauthorized")
        authToken = authenticate()
        fn(authToken)
    }
}
