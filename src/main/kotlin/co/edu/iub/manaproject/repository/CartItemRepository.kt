package co.edu.iub.manaproject.repository

import co.edu.iub.manaproject.model.CartItem
import org.springframework.data.jpa.repository.JpaRepository

interface CartItemRepository : JpaRepository<CartItem, Long> {

    fun findAllByUserIdOrderByCreatedAtAsc(userId: Long): List<CartItem>

    fun findByUserIdAndProductId(userId: Long, productId: Long): CartItem?

    fun deleteAllByUserId(userId: Long)

    fun countByUserId(userId: Long): Long
}
