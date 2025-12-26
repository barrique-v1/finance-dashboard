package com.financedashboard.backend.security

import com.financedashboard.backend.config.JwtProperties
import com.financedashboard.backend.entity.User
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(private val jwtProperties: JwtProperties) {

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun generateAccessToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.accessTokenExpiration)

        return Jwts.builder()
            .subject(user.getEmail())
            .claim("roles", user.authorities.map { it.authority })
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(): String {
        return UUID.randomUUID().toString()
    }

    fun getEmailFromToken(token: String): String? {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
        } catch (e: JwtException) {
            null
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: JwtException) {
            false
        }
    }

    fun getAccessTokenExpirationMs(): Long = jwtProperties.accessTokenExpiration

    fun getRefreshTokenExpirationMs(): Long = jwtProperties.refreshTokenExpiration
}
