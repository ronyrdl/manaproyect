package co.edu.iub.manaproject.dto.order

import co.edu.iub.manaproject.model.PaymentType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class CreateOrderRequest(

    @field:Size(max = 500)
    val notes: String?,

    val paymentType: PaymentType?,

    @field:NotEmpty(message = "El pedido debe contener al menos un producto")
    @field:Valid
    val items: List<CreateOrderItemRequest> = emptyList()
)
