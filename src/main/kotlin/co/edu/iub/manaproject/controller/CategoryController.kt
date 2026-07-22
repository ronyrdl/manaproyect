package co.edu.iub.manaproject.controller

import co.edu.iub.manaproject.dto.category.CategoryResponse
import co.edu.iub.manaproject.dto.category.CategoryStatusRequest
import co.edu.iub.manaproject.dto.category.CreateCategoryRequest
import co.edu.iub.manaproject.dto.category.UpdateCategoryRequest
import co.edu.iub.manaproject.service.CategoryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
@RequestMapping("/categories")
class CategoryController(
    private val categoryService: CategoryService) {

    @GetMapping
    fun getActiveCategories(): List<CategoryResponse> {
        return categoryService.getActiveCategory()
    }

    @PostMapping
    fun createCategory(
        @Valid @RequestBody request: CreateCategoryRequest
    ): ResponseEntity<CategoryResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(categoryService.createCategory(request))
    }

    @PutMapping("/{id}")
    fun updateCategory(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCategoryRequest
    ): CategoryResponse {
        return categoryService.updateCategory(id, request)
    }

    @PatchMapping("/{id}/status")
    fun changeStatus(
        @PathVariable id: Long,
        @RequestBody request: CategoryStatusRequest
    ): CategoryResponse {
        return categoryService.changeStatus(id, request)
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: Long): CategoryResponse {
        return categoryService.deleteCategory(id)
    }
}