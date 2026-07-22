package co.edu.iub.manaproject.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "system_config")
class SystemConfig(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 100)
    var configKey: String = "",

    @Column(nullable = false, length = 500)
    var configValue: String = "",

    @Column(length = 300)
    var description: String? = null
)
