package co.edu.iub.manaproject.dto.category

data class CategoryResponse (
    
    val id: Long,
    val name: String,
    val description: String?,
    val active: Boolean
)