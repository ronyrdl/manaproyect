package co.edu.iub.manaproject.dto.stats

import java.math.BigDecimal

data class DailyStatsResponse(
    val date: String,
    val totalOrders: Long,
    val totalRevenue: BigDecimal,
    val totalProductsSold: Long,
    val completedOrders: Long,
    val cancelledOrders: Long
)
