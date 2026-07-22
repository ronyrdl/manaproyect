package co.edu.iub.manaproject.dto.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateUserRequest (

    @field:NotBlank
    @field:Size(max = 80)
    val firstName: String,

    @field:NotBlank
    @field:Size(max = 80)
    val lastName: String,

    @field:NotBlank
    @field:Size(min = 3, max = 50)
    val username: String,

    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = 8, max = 100)
    val password: String
)
