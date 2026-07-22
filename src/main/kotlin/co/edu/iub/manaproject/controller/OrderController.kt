package co.edu.iub.manaproject.controller

import co.edu.iub.manaproject.dto.MessageResponse
import co.edu.iub.manaproject.dto.order.CreateOrderRequest
import co.edu.iub.manaproject.dto.order.OrderResponse
import co.edu.iub.manaproject.dto.order.OrderStatusRequest
import co.edu.iub.manaproject.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    fun createOrder(
        @Valid @RequestBody request: CreateOrderRequest,
        authentication: Authentication
    ): ResponseEntity<OrderResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(orderService.createOrder(authentication.name, request))
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllOrders(): List<OrderResponse> {
        return orderService.getOrders()
    }

    @GetMapping("/my-orders")
    fun getMyOrders(authentication: Authentication): List<OrderResponse> {
        return orderService.getUserOrders(authentication.name)
    }

    @GetMapping("/{id}")
    fun getOrderById(@PathVariable id: Long): OrderResponse {
        return orderService.getOrderById(id)
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: OrderStatusRequest
    ): OrderResponse {
        return orderService.updateStatus(id, request)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteOrder(
        @PathVariable id: Long,
        authentication: Authentication
    ): OrderResponse {
        return orderService.deleteOrder(id, authentication.name)
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun clearAllOrders(authentication: Authentication): ResponseEntity<MessageResponse> {
        val count = orderService.clearAllOrders(authentication.name)
        return ResponseEntity.ok(MessageResponse("Se eliminaron $count pedido(s)"))
    }
}
