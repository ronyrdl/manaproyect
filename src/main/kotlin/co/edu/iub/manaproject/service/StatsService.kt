package co.edu.iub.manaproject.service

import co.edu.iub.manaproject.dto.stats.DailyStatsResponse
import co.edu.iub.manaproject.model.OrderStatus
import co.edu.iub.manaproject.repository.OrderRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class StatsService(
    private val orderRepository: OrderRepository
) {

    @Transactional(readOnly = true)
    fun getDailyStats(date: String): DailyStatsResponse {
        val day = LocalDate.parse(date)
        val start = day.atStartOfDay()
        val end = day.atTime(LocalTime.MAX)

        val orders = orderRepository.findAllByCreatedAtBetweenAndActiveTrue(start, end)

        val totalOrders = orders.size.toLong()
        val completedOrders = orders.count { it.status == OrderStatus.COMPLETED }.toLong()
        val cancelledOrders = orders.count { it.status == OrderStatus.CANCELLED }.toLong()
        val totalRevenue = orders
            .filter { it.status == OrderStatus.COMPLETED }
            .sumOf { it.total }
            .let { BigDecimal.valueOf(it.toDouble()) }
        val totalProductsSold = orders
            .filter { it.status == OrderStatus.COMPLETED }
            .sumOf { order -> order.items.sumOf { it.quantity } }
            .toLong()

        return DailyStatsResponse(
            date = date,
            totalOrders = totalOrders,
            totalRevenue = totalRevenue,
            totalProductsSold = totalProductsSold,
            completedOrders = completedOrders,
            cancelledOrders = cancelledOrders
        )
    }

    @Transactional(readOnly = true)
    fun getStatsBetween(startDate: String, endDate: String): List<DailyStatsResponse> {
        val start = LocalDate.parse(startDate).atStartOfDay()
        val end = LocalDate.parse(endDate).atTime(LocalTime.MAX)

        val orders = orderRepository.findAllByCreatedAtBetweenAndActiveTrue(start, end)

        val ordersByDay = orders.groupBy { it.createdAt.toLocalDate() }

        return ordersByDay.map { (date, dayOrders) ->
            val completedOrders = dayOrders.count { it.status == OrderStatus.COMPLETED }.toLong()
            val cancelledOrders = dayOrders.count { it.status == OrderStatus.CANCELLED }.toLong()
            val totalRevenue = dayOrders
                .filter { it.status == OrderStatus.COMPLETED }
                .sumOf { it.total }
                .let { BigDecimal.valueOf(it.toDouble()) }
            val totalProductsSold = dayOrders
                .filter { it.status == OrderStatus.COMPLETED }
                .sumOf { order -> order.items.sumOf { it.quantity } }
                .toLong()

            DailyStatsResponse(
                date = date.toString(),
                totalOrders = dayOrders.size.toLong(),
                totalRevenue = totalRevenue,
                totalProductsSold = totalProductsSold,
                completedOrders = completedOrders,
                cancelledOrders = cancelledOrders
            )
        }.sortedBy { it.date }
    }
}
