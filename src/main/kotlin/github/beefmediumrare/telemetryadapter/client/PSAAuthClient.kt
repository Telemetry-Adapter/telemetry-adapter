package github.beefmediumrare.telemetryadapter.client

import com.fasterxml.jackson.annotation.JsonProperty
import feign.Headers
import feign.Param
import feign.RequestLine

interface PSAAuthClient {

    @RequestLine("POST /oauth2/access_token?realm={realm}&grant_type={grantType}&username={username}&password={password}&scope={scope}")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Authorization: Basic {basicAuthToken}",
    )
    fun authenticate(
        @Param("realm") realm: String,
        @Param("grantType") grantType: String,
        @Param("username") username: String,
        @Param("password") password: String,
        @Param("scope") scope: String,
        @Param("basicAuthToken") basicAuthToken: String,
    ): PostAccessTokenResponse

    data class PostAccessTokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,
    )
}
