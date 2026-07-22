package co.edu.iub.manaproject.service

import co.edu.iub.manaproject.dto.audit.AuditLogResponse
import co.edu.iub.manaproject.model.AuditLog
import co.edu.iub.manaproject.repository.AuditLogRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

@Service
class AuditService(
    private val auditLogRepository: AuditLogRepository
) {

    @Transactional
    fun log(action: String, entityType: String, entityId: Long, username: String, details: String? = null) {
        val log = AuditLog(
            action = action,
            entityType = entityType,
            entityId = entityId,
            username = username,
            details = details
        )
        auditLogRepository.save(log)
    }

    @Transactional(readOnly = true)
    fun getAllLogs(): List<AuditLogResponse> {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().map { toResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getLogsByUser(username: String): List<AuditLogResponse> {
        return auditLogRepository.findAllByUsernameOrderByCreatedAtDesc(username).map { toResponse(it) }
    }

    private fun toResponse(log: AuditLog): AuditLogResponse {
        return AuditLogResponse(
            id = requireNotNull(log.id),
            action = log.action,
            entityType = log.entityType,
            entityId = log.entityId,
            username = log.username,
            details = log.details,
            createdAt = log.createdAt
        )
    }
}
