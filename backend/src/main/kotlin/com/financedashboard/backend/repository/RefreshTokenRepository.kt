package com.financedashboard.backend.repository

import com.financedashboard.backend.entity.RefreshToken
import com.financedashboard.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByToken(token: String): RefreshToken?
    fun deleteByUser(user: User)
}
