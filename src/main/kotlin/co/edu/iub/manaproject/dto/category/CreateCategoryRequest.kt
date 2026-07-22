package co.edu.iub.manaproject.dto.category

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateCategoryRequest (

    @field:NotBlank
    @field:Size(max = 100)
    val name: String,

    @field:Size(max = 300)
    val description: String?
)