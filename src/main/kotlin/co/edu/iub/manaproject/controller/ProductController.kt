package co.edu.iub.manaproject.controller

import co.edu.iub.manaproject.dto.product.CreateProductRequest
import co.edu.iub.manaproject.dto.product.ProductResponse
import co.edu.iub.manaproject.dto.product.ProductStatusRequest
import co.edu.iub.manaproject.dto.product.UpdateProductRequest
import co.edu.iub.manaproject.service.ProductService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping
    fun getProducts(): List<ProductResponse> {
        return productService.getProducts()
    }

    @PostMapping
    fun createProduct(
        @Valid @RequestBody request: CreateProductRequest
    ): ProductResponse {
        return productService.createProduct(request)
    }

    @PutMapping("/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProductRequest
    ): ProductResponse {
        return productService.updateProduct(id, request)
    }

    @PatchMapping("/{id}/status")
    fun changeStatus(
        @PathVariable id: Long,
        @RequestBody request: ProductStatusRequest
    ): ProductResponse {
        return productService.changeStatus(id, request.active)
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(
        @PathVariable id: Long
    ): ProductResponse {
        return productService.deleteProduct(id)
    }
}
