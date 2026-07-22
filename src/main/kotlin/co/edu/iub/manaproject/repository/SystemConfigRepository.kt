package co.edu.iub.manaproject.repository

import co.edu.iub.manaproject.model.SystemConfig
import org.springframework.data.jpa.repository.JpaRepository

interface SystemConfigRepository : JpaRepository<SystemConfig, Long> {

    fun findByConfigKey(configKey: String): SystemConfig?

    fun existsByConfigKeyIgnoreCase(configKey: String): Boolean
}
