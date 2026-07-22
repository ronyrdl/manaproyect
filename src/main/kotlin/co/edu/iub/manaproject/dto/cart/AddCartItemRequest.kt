package co.edu.iub.manaproject.dto.cart

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class AddCartItemRequest(

    @field:NotNull(message = "El producto es obligatorio")
    val productId: Long,

    @field:Min(value = 1, message = "La cantidad debe ser al menos 1")
    val quantity: Int = 1,

    @field:Size(max = 300)
    val notes: String? = null
)
