package com.storyous.contacts

import com.auth0.android.jwt.JWT

object JWTWrapper {

    var decoder: (String) -> JWTClaims = AndroidJWTDecoder()

    @Suppress("FunctionNaming")
    fun AndroidJWTDecoder(): (String) -> JWTClaims = { token ->
        val decode = JWT(token)

        decode.getClaim("claims").asObject(JWTClaims::class.java)
            ?: JWTClaims(
                decode.getClaim("merchantId").asString(),
                decode.getClaim("placeId").asString()
            )
    }

    fun decode(token: String): JWTClaims = decoder(token)
}
