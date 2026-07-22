package co.edu.iub.manaproject.dto.user

import co.edu.iub.manaproject.model.UserRole

data class UserResponse (
    val id: Long,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val role: UserRole,
    val active: Boolean
)