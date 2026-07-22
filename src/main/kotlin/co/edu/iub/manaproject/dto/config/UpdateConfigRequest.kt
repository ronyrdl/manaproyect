package co.edu.iub.manaproject.dto.config

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateConfigRequest(

    @field:NotBlank
    @field:Size(max = 500)
    val configValue: String,

    @field:Size(max = 300)
    val description: String?
)
