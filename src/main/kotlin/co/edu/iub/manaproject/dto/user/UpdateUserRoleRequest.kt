package co.edu.iub.manaproject.dto.user

import co.edu.iub.manaproject.model.UserRole
import org.jetbrains.annotations.NotNull

data class UpdateUserRoleRequest (

    @field: NotNull
    val role: UserRole,
)