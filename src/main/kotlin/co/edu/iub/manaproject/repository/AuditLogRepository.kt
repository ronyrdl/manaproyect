package co.edu.iub.manaproject.repository

import co.edu.iub.manaproject.model.AuditLog
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface AuditLogRepository : JpaRepository<AuditLog, Long> {

    fun findAllByOrderByCreatedAtDesc(): List<AuditLog>

    fun findAllByCreatedAtBetweenOrderByCreatedAtDesc(start: LocalDateTime, end: LocalDateTime): List<AuditLog>

    fun findAllByUsernameOrderByCreatedAtDesc(username: String): List<AuditLog>
}
