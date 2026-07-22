package co.edu.iub.manaproject.controller

import co.edu.iub.manaproject.dto.MessageResponse
import co.edu.iub.manaproject.dto.cart.AddCartItemRequest
import co.edu.iub.manaproject.dto.cart.CartItemResponse
import co.edu.iub.manaproject.dto.cart.UpdateCartItemRequest
import co.edu.iub.manaproject.service.CartService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
@RequestMapping("/cart")
class CartController(
    private val cartService: CartService
) {

    @GetMapping
    fun getCart(authentication: Authentication): List<CartItemResponse> {
        return cartService.getCart(authentication.name)
    }

    @GetMapping("/count")
    fun getCartCount(authentication: Authentication): ResponseEntity<Map<String, Long>> {
        val count = cartService.getCartCount(authentication.name)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @PostMapping
    fun addItem(
        @Valid @RequestBody request: AddCartItemRequest,
        authentication: Authentication
    ): ResponseEntity<CartItemResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(cartService.addItem(authentication.name, request))
    }

    @PatchMapping("/{id}")
    fun updateItem(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCartItemRequest,
        authentication: Authentication
    ): CartItemResponse {
        return cartService.updateItem(id, authentication.name, request)
    }

    @DeleteMapping("/{id}")
    fun removeItem(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<MessageResponse> {
        cartService.removeItem(id, authentication.name)
        return ResponseEntity.ok(MessageResponse("Item eliminado del carrito"))
    }

    @DeleteMapping
    fun clearCart(authentication: Authentication): ResponseEntity<MessageResponse> {
        cartService.clearCart(authentication.name)
        return ResponseEntity.ok(MessageResponse("Carrito limpiado"))
    }
}
