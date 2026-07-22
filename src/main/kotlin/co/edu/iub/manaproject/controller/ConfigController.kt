package co.edu.iub.manaproject.controller

import co.edu.iub.manaproject.dto.MessageResponse
import co.edu.iub.manaproject.dto.config.ConfigResponse
import co.edu.iub.manaproject.dto.config.UpdateConfigRequest
import co.edu.iub.manaproject.service.ConfigService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/config")
@PreAuthorize("hasRole('ADMIN')")
class ConfigController(
    private val configService: ConfigService
) {

    @GetMapping
    fun getAllConfigs(): List<ConfigResponse> {
        return configService.getAllConfigs()
    }

    @GetMapping("/{key}")
    fun getConfigByKey(@PathVariable key: String): ConfigResponse {
        return configService.getConfigByKey(key)
    }

    @PostMapping
    fun createConfig(
        @RequestParam key: String,
        @RequestParam value: String,
        @RequestParam(required = false) description: String?
    ): ResponseEntity<ConfigResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(configService.createConfig(key, value, description))
    }

    @PutMapping("/{id}")
    fun updateConfig(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateConfigRequest
    ): ConfigResponse {
        return configService.updateConfig(id, request)
    }

    @DeleteMapping("/{id}")
    fun deleteConfig(@PathVariable id: Long): ResponseEntity<MessageResponse> {
        configService.deleteConfig(id)
        return ResponseEntity.ok(MessageResponse("Configuración eliminada"))
    }
}
