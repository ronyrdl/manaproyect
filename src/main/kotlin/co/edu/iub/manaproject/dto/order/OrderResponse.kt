package co.edu.iub.manaproject.dto.order

import co.edu.iub.manaproject.model.OrderStatus
import co.edu.iub.manaproject.model.PaymentType
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderResponse(
    val id: Long,
    val orderNumber: String,
    val status: OrderStatus,
    val total: BigDecimal,
    val notes: String?,
    val userName: String,
    val userId: Long,
    val paymentType: PaymentType?,
    val items: List<OrderItemResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
