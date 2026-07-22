package co.edu.iub.manaproject.dto.config

data class ConfigResponse(
    val id: Long,
    val configKey: String,
    val configValue: String,
    val description: String?
)
