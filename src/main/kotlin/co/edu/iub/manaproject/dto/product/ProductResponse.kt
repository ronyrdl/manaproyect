package co.edu.iub.manaproject.dto.product

import java.math.BigDecimal

data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val active: Boolean,
    val categoryId: Long,
    val categoryName: String
)