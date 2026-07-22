package co.edu.iub.manaproject.dto.cart

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class UpdateCartItemRequest(

    @field:Min(value = 1, message = "La cantidad debe ser al menos 1")
    val quantity: Int,

    @field:Size(max = 300)
    val notes: String? = null
)
