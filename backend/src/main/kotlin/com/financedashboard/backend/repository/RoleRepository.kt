package com.financedashboard.backend.repository

import com.financedashboard.backend.entity.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Int> {
    fun findByName(name: String): Role?
}
