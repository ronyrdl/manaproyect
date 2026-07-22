package co.edu.iub.manaproject.service

import co.edu.iub.manaproject.dto.category.CategoryResponse
import co.edu.iub.manaproject.dto.category.CategoryStatusRequest
import co.edu.iub.manaproject.dto.category.CreateCategoryRequest
import co.edu.iub.manaproject.dto.category.UpdateCategoryRequest
import co.edu.iub.manaproject.exception.DuplicateResourceException
import co.edu.iub.manaproject.exception.InvalidRequestException
import co.edu.iub.manaproject.model.Category
import co.edu.iub.manaproject.repository.CategoryRepository
import co.edu.iub.manaproject.exception.ResourceNotFoundException
import co.edu.iub.manaproject.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class CategoryService (
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository
) {
    fun getActiveCategory(): List<CategoryResponse> {
        return categoryRepository.findAllByActiveTrueOrderByNameAsc()
            .map { toResponse(it)}
    }

    fun createCategory(request: CreateCategoryRequest): CategoryResponse {
        val name = request.name.trim()

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw DuplicateResourceException("Categoria ya existente")
        }
        val category = Category(
            name = name,
            description = request.description?.trim(),
        )
        return toResponse(categoryRepository.save(category))
    }

    fun updateCategory(id: Long, request: UpdateCategoryRequest): CategoryResponse {
        val category = findCategory(id)
        val name = request.name.trim()

        if (!category.name.equals(name, ignoreCase = true) && categoryRepository.existsByNameIgnoreCase(name)) {
            throw DuplicateResourceException("Categoria ya existente")

        }
        category.name = name
        category.description = request.description?.trim()

        return toResponse(categoryRepository.save(category))
    }

    fun changeStatus(id: Long, request: CategoryStatusRequest): CategoryResponse {
        val category = findCategory(id)
        category.active = request.active

        return toResponse(categoryRepository.save(category))
    }

    private fun findCategory(id: Long): Category {
        return categoryRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException("Categoría no encontrada")
            }
    }
    fun deleteCategory(id: Long): CategoryResponse {
        val category = findCategory(id)

        val hasProducts = productRepository.findAllByActiveTrueAndCategoryActiveTrueOrderByCategoryNameAscNameAsc()
            .any { it.category?.id == id }

        if (hasProducts) {
            throw InvalidRequestException("No se puede eliminar la categoría porque tiene productos asociados")
        }

        category.active = false

        return toResponse(categoryRepository.save(category))
    }

    private fun toResponse(category: Category): CategoryResponse {
        return CategoryResponse(
            id = requireNotNull(category.id),
            name = category.name,
            description = category.description,
            active = category.active
        )
    }
}