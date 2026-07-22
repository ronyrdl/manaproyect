package co.edu.iub.manaproject.service

import co.edu.iub.manaproject.dto.order.CreateOrderItemRequest
import co.edu.iub.manaproject.dto.order.CreateOrderRequest
import co.edu.iub.manaproject.dto.order.OrderItemResponse
import co.edu.iub.manaproject.dto.order.OrderResponse
import co.edu.iub.manaproject.dto.order.OrderStatusRequest
import co.edu.iub.manaproject.exception.InvalidRequestException
import co.edu.iub.manaproject.exception.ResourceNotFoundException
import co.edu.iub.manaproject.model.Order
import co.edu.iub.manaproject.model.OrderItem
import co.edu.iub.manaproject.model.OrderStatus
import co.edu.iub.manaproject.model.PaymentType
import co.edu.iub.manaproject.model.Product
import co.edu.iub.manaproject.model.User
import co.edu.iub.manaproject.repository.OrderRepository
import co.edu.iub.manaproject.repository.ProductRepository
import co.edu.iub.manaproject.repository.UserRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val auditService: AuditService
) {

    @Transactional
    fun createOrder(username: String, request: CreateOrderRequest): OrderResponse {
        val user = findUserByUsername(username)

        val items = request.items.map { validateAndCreateItem(it) }

        val total = items.sumOf { it.subtotal }

        if (total <= BigDecimal.ZERO) {
            throw InvalidRequestException("El total del pedido debe ser mayor que cero")
        }

        val orderNumber = "ORD-${System.currentTimeMillis()}"

        val order = Order(
            orderNumber = orderNumber,
            status = OrderStatus.PENDING,
            total = total,
            notes = request.notes?.trim(),
            user = user,
            paymentType = request.paymentType
        )

        val savedOrder = orderRepository.save(order)

        items.forEach { item ->
            item.order = savedOrder
        }
        savedOrder.items.addAll(items)

        val finalOrder = orderRepository.save(savedOrder)

        auditService.log("CREATE", "Order", requireNotNull(finalOrder.id), username, "Pedido $orderNumber creado con ${items.size} producto(s)")

        return toResponse(finalOrder)
    }

    @Transactional(readOnly = true)
    fun getOrders(): List<OrderResponse> {
        return orderRepository.findAllByActiveTrueOrderByCreatedAtDesc().map { toResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getUserOrders(username: String): List<OrderResponse> {
        val user = findUserByUsername(username)
        return orderRepository.findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(requireNotNull(user.id)).map { toResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getOrderById(id: Long): OrderResponse {
        val order = findOrder(id)
        return toResponse(order)
    }

    @Transactional
    fun updateStatus(id: Long, request: OrderStatusRequest): OrderResponse {
        val order = findOrder(id)

        val newStatus = request.status
        val currentStatus = order.status

        val validTransitions = mapOf(
            OrderStatus.PENDING to listOf(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED to listOf(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING to listOf(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
            OrderStatus.COMPLETED to emptyList(),
            OrderStatus.CANCELLED to emptyList()
        )

        if (newStatus !in (validTransitions[currentStatus] ?: emptyList())) {
            throw InvalidRequestException(
                "No se puede cambiar de ${currentStatus.name} a ${newStatus.name}"
            )
        }

        order.status = newStatus
        order.updatedAt = LocalDateTime.now()

        val saved = orderRepository.save(order)

        auditService.log("UPDATE_STATUS", "Order", id, saved.user?.username ?: "unknown", "Estado cambiado de ${currentStatus.name} a ${newStatus.name}")

        return toResponse(saved)
    }

    @Transactional
    fun deleteOrder(id: Long, username: String): OrderResponse {
        val order = findOrder(id)

        order.active = false
        order.updatedAt = LocalDateTime.now()

        val saved = orderRepository.save(order)

        auditService.log("DELETE", "Order", id, username, "Pedido ${order.orderNumber} eliminado")

        return toResponse(saved)
    }

    @Transactional
    fun clearAllOrders(username: String): Long {
        val orders = orderRepository.findAllByActiveTrueOrderByCreatedAtDesc()
        val count = orders.size

        orders.forEach { order ->
            order.active = false
            order.updatedAt = LocalDateTime.now()
        }

        orderRepository.saveAll(orders)

        auditService.log("CLEAR_ALL", "Order", 0, username, "Limpieza total: $count pedidos eliminados")

        return count.toLong()
    }

    private fun validateAndCreateItem(request: CreateOrderItemRequest): OrderItem {
        val product = productRepository.findById(request.productId)
            .orElseThrow { ResourceNotFoundException("Producto no encontrado") }

        if (!product.active) {
            throw InvalidRequestException("El producto '${product.name}' no está disponible")
        }

        if (product.category == null || !product.category!!.active) {
            throw InvalidRequestException("La categoría del producto '${product.name}' no está activa")
        }

        if (request.unitPrice < BigDecimal.ZERO) {
            throw InvalidRequestException("El precio no puede ser negativo")
        }

        val quantity = if (request.quantity < 1) 1 else request.quantity
        val subtotal = request.unitPrice.multiply(BigDecimal.valueOf(quantity.toLong()))

        return OrderItem(
            product = product,
            quantity = quantity,
            unitPrice = request.unitPrice,
            subtotal = subtotal,
            notes = request.notes?.trim()
        )
    }

    private fun findOrder(id: Long): Order {
        return orderRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Pedido no encontrado") }
    }

    private fun findUserByUsername(username: String): User {
        return userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
    }

    private fun toResponse(order: Order): OrderResponse {
        return OrderResponse(
            id = requireNotNull(order.id),
            orderNumber = order.orderNumber,
            status = order.status,
            total = order.total,
            notes = order.notes,
            userName = order.user?.username ?: "",
            userId = requireNotNull(order.user?.id),
            paymentType = order.paymentType,
            items = order.items.map { item ->
                OrderItemResponse(
                    id = requireNotNull(item.id),
                    productId = requireNotNull(item.product?.id),
                    productName = item.product?.name ?: "",
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    subtotal = item.subtotal,
                    notes = item.notes
                )
            },
            createdAt = order.createdAt,
            updatedAt = order.updatedAt
        )
    }
}
