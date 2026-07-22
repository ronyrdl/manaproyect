package co.edu.iub.manaproject.service

import co.edu.iub.manaproject.dto.product.CreateProductRequest
import co.edu.iub.manaproject.dto.product.ProductResponse
import co.edu.iub.manaproject.dto.product.UpdateProductRequest
import co.edu.iub.manaproject.exception.ResourceNotFoundException
import co.edu.iub.manaproject.model.Category
import co.edu.iub.manaproject.model.Product
import co.edu.iub.manaproject.repository.CategoryRepository
import co.edu.iub.manaproject.repository.ProductRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {

    @Transactional(readOnly = true)
    fun getProducts(): List<ProductResponse> {
        return productRepository.findAllByOrderByNameAsc().map { toResponse(it) }
    }

    fun createProduct(request: CreateProductRequest): ProductResponse {
        val category = findCategory(request.categoryId)

        val product = Product(
            name = request.name.trim(),
            description = request.description?.trim(),
            price = request.price,
            category = category
        )

        return toResponse(productRepository.save(product))
    }

    fun updateProduct(id: Long, request: UpdateProductRequest): ProductResponse {
        val product = findProduct(id)
        val category = findCategory(request.categoryId)

        product.name = request.name.trim()
        product.description = request.description?.trim()
        product.price = request.price
        product.category = category

        return toResponse(productRepository.save(product))
    }

    fun changeStatus(id: Long, active: Boolean): ProductResponse {
        val product = findProduct(id)

        product.active = active

        return toResponse(productRepository.save(product))
    }

    fun deleteProduct(id: Long): ProductResponse {
        val product = findProduct(id)

        product.active = false

        return toResponse(productRepository.save(product))
    }

    private fun findProduct(id: Long): Product {
        return productRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException("Producto no encontrado")
            }
    }

    private fun findCategory(id: Long): Category {
        return categoryRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException("Categoría no encontrada")
            }
    }

    private fun toResponse(product: Product): ProductResponse {
        val category = requireNotNull(product.category) {
            "El producto no tiene una categoría asignada"
        }

        return ProductResponse(
            id = requireNotNull(product.id),
            name = product.name,
            description = product.description,
            price = product.price,
            active = product.active,
            categoryId = requireNotNull(category.id),
            categoryName = category.name
        )
    }
}