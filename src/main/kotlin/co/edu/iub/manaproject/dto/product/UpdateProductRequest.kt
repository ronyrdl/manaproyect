package co.edu.iub.manaproject.dto.product

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class UpdateProductRequest(

    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    val name: String,

    @field:Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    val description: String?,

    @field:NotNull(message = "El precio es obligatorio")
    @field:DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
    val price: BigDecimal,

    @field:NotNull(message = "La categoría es obligatoria")
    val categoryId: Long
)