package co.edu.iub.manaproject.repository

import co.edu.iub.manaproject.model.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository: JpaRepository<Category, Long> {

    fun existsByNameIgnoreCase(name: String): Boolean
    
    fun findAllByActiveTrueOrderByNameAsc(): List<Category>
}