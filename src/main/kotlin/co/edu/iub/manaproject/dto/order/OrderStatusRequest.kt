package co.edu.iub.manaproject.dto.order

import co.edu.iub.manaproject.model.OrderStatus
import jakarta.validation.constraints.NotNull

data class OrderStatusRequest(

    @field:NotNull
    val status: OrderStatus
)
