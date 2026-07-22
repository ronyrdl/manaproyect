package co.edu.iub.manaproject.service

import co.edu.iub.manaproject.dto.config.ConfigResponse
import co.edu.iub.manaproject.dto.config.UpdateConfigRequest
import co.edu.iub.manaproject.exception.DuplicateResourceException
import co.edu.iub.manaproject.exception.ResourceNotFoundException
import co.edu.iub.manaproject.model.SystemConfig
import co.edu.iub.manaproject.repository.SystemConfigRepository
import org.springframework.stereotype.Service

@Service
class ConfigService(
    private val systemConfigRepository: SystemConfigRepository
) {

    fun getAllConfigs(): List<ConfigResponse> {
        return systemConfigRepository.findAll().map { toResponse(it) }
    }

    fun getConfigByKey(key: String): ConfigResponse {
        val config = systemConfigRepository.findByConfigKey(key)
            ?: throw ResourceNotFoundException("Configuración no encontrada")
        return toResponse(config)
    }

    fun createConfig(key: String, value: String, description: String? = null): ConfigResponse {
        if (systemConfigRepository.existsByConfigKeyIgnoreCase(key)) {
            throw DuplicateResourceException("La configuración '$key' ya existe")
        }

        val config = SystemConfig(
            configKey = key.trim(),
            configValue = value.trim(),
            description = description?.trim()
        )

        return toResponse(systemConfigRepository.save(config))
    }

    fun updateConfig(id: Long, request: UpdateConfigRequest): ConfigResponse {
        val config = systemConfigRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Configuración no encontrada") }

        config.configValue = request.configValue.trim()
        config.description = request.description?.trim()

        return toResponse(systemConfigRepository.save(config))
    }

    fun deleteConfig(id: Long) {
        val config = systemConfigRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Configuración no encontrada") }
        systemConfigRepository.delete(config)
    }

    private fun toResponse(config: SystemConfig): ConfigResponse {
        return ConfigResponse(
            id = requireNotNull(config.id),
            configKey = config.configKey,
            configValue = config.configValue,
            description = config.description
        )
    }
}
