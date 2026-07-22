package co.edu.iub.manaproject.dto.menu

import java.math.BigDecimal

data class MenuProductResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val categoryId: Long,
    val categoryName: String
)