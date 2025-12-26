package com.financedashboard.backend.service

import com.financedashboard.backend.dto.AuthResponse
import com.financedashboard.backend.dto.LoginRequest
import com.financedashboard.backend.dto.RegisterRequest
import com.financedashboard.backend.entity.RefreshToken
import com.financedashboard.backend.entity.User
import com.financedashboard.backend.repository.RefreshTokenRepository
import com.financedashboard.backend.repository.RoleRepository
import com.financedashboard.backend.repository.UserRepository
import com.financedashboard.backend.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already registered")
        }

        val userRole = roleRepository.findByName("ROLE_USER")
            ?: throw IllegalStateException("Default role not found")

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )
        user.roles.add(userRole)

        val savedUser = userRepository.save(user)
        return createTokens(savedUser)
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("User not found")

        return createTokens(user)
    }

    @Transactional
    fun refresh(refreshToken: String): AuthResponse {
        val token = refreshTokenRepository.findByToken(refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")

        if (token.isExpired()) {
            refreshTokenRepository.delete(token)
            throw IllegalArgumentException("Refresh token expired")
        }

        val user = token.user
        refreshTokenRepository.delete(token)

        return createTokens(user)
    }

    @Transactional
    fun logout(refreshToken: String) {
        val token = refreshTokenRepository.findByToken(refreshToken)
        if (token != null) {
            refreshTokenRepository.delete(token)
        }
    }

    private fun createTokens(user: User): AuthResponse {
        val accessToken = jwtTokenProvider.generateAccessToken(user)
        val refreshTokenValue = jwtTokenProvider.generateRefreshToken()

        val refreshToken = RefreshToken(
            user = user,
            token = refreshTokenValue,
            expiresAt = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs())
        )
        refreshTokenRepository.save(refreshToken)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshTokenValue,
            expiresIn = jwtTokenProvider.getAccessTokenExpirationMs()
        )
    }
}
