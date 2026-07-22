package co.edu.iub.manaproject.dto.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class UpdateProfileRequest (


    @field:Email("El correo no tiene un formato válido")
    val email: String?,

    @field:Size(min=1, max=80)
    val firstName: String?,

    @field:Size(min=1, max=80)
    val lastName: String?,

)