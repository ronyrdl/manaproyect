package co.edu.iub.manaproject.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.Email
import java.time.LocalDateTime


@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 80)
    var firstName: String = "",

    @Column(nullable = false, length = 80)
    var lastName: String = "",

    @Column(nullable = false, unique = true,length = 80)
    var username: String = "",

    @Column(nullable = false, unique = true, length = 80)
    var email: String = "",

    @Column(nullable = false)
    var passwordHash: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: UserRole = UserRole.CUSTOMER,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
)
