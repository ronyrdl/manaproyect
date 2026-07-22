package co.edu.iub.manaproject.repository

import co.edu.iub.manaproject.model.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository: JpaRepository<Product, Long> {

    fun findAllByOrderByNameAsc(): List<Product>
    fun findAllByActiveTrueAndCategoryActiveTrueOrderByCategoryNameAscNameAsc(): List<Product>
    fun findAllByActiveTrueAndCategoryActiveTrueAndCategoryIdOrderByNameAsc(
        categoryId: Long
    ): List<Product>
}
