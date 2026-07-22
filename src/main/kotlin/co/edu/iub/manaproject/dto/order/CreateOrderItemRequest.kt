package co.edu.iub.manaproject.dto.order

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class CreateOrderItemRequest(

    @field:NotNull(message = "El producto es obligatorio")
    val productId: Long,

    @field:Min(value = 1, message = "La cantidad debe ser al menos 1")
    val quantity: Int = 1,

    @field:DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor que cero")
    val unitPrice: BigDecimal,

    @field:Size(max = 300)
    val notes: String? = null
)
