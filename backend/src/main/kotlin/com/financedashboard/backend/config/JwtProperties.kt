package com.financedashboard.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpiration: Long = 900000,      // 15 minutes
    val refreshTokenExpiration: Long = 604800000   // 7 days
)
