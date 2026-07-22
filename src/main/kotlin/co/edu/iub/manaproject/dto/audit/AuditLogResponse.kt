package co.edu.iub.manaproject.dto.audit

import java.time.LocalDateTime

data class AuditLogResponse(
    val id: Long,
    val action: String,
    val entityType: String,
    val entityId: Long,
    val username: String,
    val details: String?,
    val createdAt: LocalDateTime
)
