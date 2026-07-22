package co.edu.iub.manaproject.service

import co.edu.iub.manaproject.dto.cart.AddCartItemRequest
import co.edu.iub.manaproject.dto.cart.CartItemResponse
import co.edu.iub.manaproject.dto.cart.UpdateCartItemRequest
import co.edu.iub.manaproject.exception.InvalidRequestException
import co.edu.iub.manaproject.exception.ResourceNotFoundException
import co.edu.iub.manaproject.model.CartItem
import co.edu.iub.manaproject.model.User
import co.edu.iub.manaproject.repository.CartItemRepository
import co.edu.iub.manaproject.repository.ProductRepository
import co.edu.iub.manaproject.repository.UserRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class CartService(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getCart(username: String): List<CartItemResponse> {
        val user = findUserByUsername(username)
        return cartItemRepository.findAllByUserIdOrderByCreatedAtAsc(requireNotNull(user.id)).map { toResponse(it) }
    }

    @Transactional
    fun addItem(username: String, request: AddCartItemRequest): CartItemResponse {
        val user = findUserByUsername(username)
        val userId = requireNotNull(user.id)

        val product = productRepository.findById(request.productId)
            .orElseThrow { ResourceNotFoundException("Producto no encontrado") }

        if (!product.active || product.category == null || !product.category!!.active) {
            throw InvalidRequestException("El producto no está disponible para la venta")
        }

        val existing = cartItemRepository.findByUserIdAndProductId(userId, request.productId)

        if (existing != null) {
            existing.quantity += request.quantity
            existing.notes = request.notes?.trim()
            return toResponse(cartItemRepository.save(existing))
        }

        val cartItem = CartItem(
            user = user,
            product = product,
            quantity = request.quantity,
            notes = request.notes?.trim()
        )

        return toResponse(cartItemRepository.save(cartItem))
    }

    @Transactional
    fun updateItem(id: Long, username: String, request: UpdateCartItemRequest): CartItemResponse {
        val user = findUserByUsername(username)
        val item = findCartItem(id)

        if (item.user?.id != user.id) {
            throw ResourceNotFoundException("Item no encontrado")
        }

        item.quantity = request.quantity
        item.notes = request.notes?.trim()

        return toResponse(cartItemRepository.save(item))
    }

    @Transactional
    fun removeItem(id: Long, username: String) {
        val user = findUserByUsername(username)
        val item = findCartItem(id)

        if (item.user?.id != user.id) {
            throw ResourceNotFoundException("Item no encontrado")
        }

        cartItemRepository.delete(item)
    }

    @Transactional
    fun clearCart(username: String) {
        val user = findUserByUsername(username)
        cartItemRepository.deleteAllByUserId(requireNotNull(user.id))
    }

    @Transactional(readOnly = true)
    fun getCartCount(username: String): Long {
        val user = findUserByUsername(username)
        return cartItemRepository.countByUserId(requireNotNull(user.id))
    }

    private fun findCartItem(id: Long): CartItem {
        return cartItemRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Item del carrito no encontrado") }
    }

    private fun findUserByUsername(username: String): User {
        return userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
    }

    private fun toResponse(item: CartItem): CartItemResponse {
        val product = requireNotNull(item.product)
        val quantity = if (item.quantity < 1) 1 else item.quantity
        val subtotal = product.price.multiply(BigDecimal.valueOf(quantity.toLong()))

        return CartItemResponse(
            id = requireNotNull(item.id),
            productId = requireNotNull(product.id),
            productName = product.name,
            productPrice = product.price,
            quantity = quantity,
            subtotal = subtotal,
            notes = item.notes
        )
    }
}
