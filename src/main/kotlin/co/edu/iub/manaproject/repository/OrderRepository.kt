package co.edu.iub.manaproject.repository

import co.edu.iub.manaproject.model.Order
import co.edu.iub.manaproject.model.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface OrderRepository : JpaRepository<Order, Long> {

    fun findAllByActiveTrueOrderByCreatedAtDesc(): List<Order>

    fun findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(userId: Long): List<Order>

    fun findByOrderNumber(orderNumber: String): Order?

    fun countByCreatedAtBetweenAndStatus(start: LocalDateTime, end: LocalDateTime, status: OrderStatus): Long

    fun findAllByCreatedAtBetweenAndActiveTrue(start: LocalDateTime, end: LocalDateTime): List<Order>
}
