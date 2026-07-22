package co.edu.iub.manaproject.controller

import co.edu.iub.manaproject.dto.audit.AuditLogResponse
import co.edu.iub.manaproject.service.AuditService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/audit")
@PreAuthorize("hasRole('ADMIN')")
class AuditController(
    private val auditService: AuditService
) {

    @GetMapping
    fun getAllLogs(): List<AuditLogResponse> {
        return auditService.getAllLogs()
    }

    @GetMapping("/user/{username}")
    fun getLogsByUser(@PathVariable username: String): List<AuditLogResponse> {
        return auditService.getLogsByUser(username)
    }
}
