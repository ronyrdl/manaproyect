package co.edu.iub.manaproject.dto.cart

import java.math.BigDecimal

data class CartItemResponse(
    val id: Long,
    val productId: Long,
    val productName: String,
    val productPrice: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal,
    val notes: String?
)
