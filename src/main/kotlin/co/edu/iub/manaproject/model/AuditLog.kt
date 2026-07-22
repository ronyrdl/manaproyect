package co.edu.iub.manaproject.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "audit_logs")
class AuditLog(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 50)
    var action: String = "",

    @Column(nullable = false, length = 50)
    var entityType: String = "",

    @Column(nullable = false)
    var entityId: Long = 0,

    @Column(nullable = false, length = 80)
    var username: String = "",

    @Column(length = 1000)
    var details: String? = null,

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
