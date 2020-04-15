package com.storyous.contacts

import com.auth0.jwt.JWT

fun JWTDecoder(): (String) -> JWTClaims = { token ->

    val decode = JWT.decode(token)
    val claims: Map<String, Any>? = decode.getClaim("claims").asMap()

    JWTClaims(
        claims?.get("merchantId") as? String ?: decode.getClaim("merchantId").asString(),
        claims?.get("placeId") as? String ?: decode.getClaim("placeId").asString()
    )
}
