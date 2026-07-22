package co.edu.iub.manaproject.dto.order

import java.math.BigDecimal

data class OrderItemResponse(
    val id: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
    val notes: String?
)
