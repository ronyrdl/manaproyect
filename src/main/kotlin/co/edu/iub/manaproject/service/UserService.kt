package co.edu.iub.manaproject.service

import co.edu.iub.manaproject.dto.user.ChangePasswordRequest
import co.edu.iub.manaproject.dto.user.CreateUserRequest
import co.edu.iub.manaproject.dto.user.UpdateProfileRequest
import co.edu.iub.manaproject.dto.user.UpdateUserRoleRequest
import co.edu.iub.manaproject.dto.user.UserResponse
import co.edu.iub.manaproject.exception.DuplicateResourceException
import co.edu.iub.manaproject.repository.UserRepository
import co.edu.iub.manaproject.exception.ResourceNotFoundException
import co.edu.iub.manaproject.model.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun getProfile(currentUsername: String): UserResponse {
        return toResponse(findUserByUsername(currentUsername))
    }

    fun updateProfile(
        currentUsername: String,
        request: UpdateProfileRequest
    ): UserResponse {
        val user = findUserByUsername(currentUsername)

        request.firstName?.let {
            user.firstName = it.trim()
        }

        request.lastName?.let {
            user.lastName = it.trim()
        }

        request.email?.let {
            val email = it.trim().lowercase()

            if (userRepository.existsByEmailAndIdNot(email, requireNotNull((user.id)))) {
                throw DuplicateResourceException("El correo ya está registrado")
            }

            user.email = email
        }

        return toResponse(userRepository.save(user))
    }

    fun updateRole(userId: Long, request: UpdateUserRoleRequest): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        user.role = request.role
        return toResponse(userRepository.save(user))
    }
    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { toResponse(it) }
    }

    private fun findUserByUsername(username: String): User {
        return userRepository.findByUsername(username)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
    }

    fun changePassword(currentUsername: String, request: ChangePasswordRequest) {
        val user = findUserByUsername(currentUsername)

        if (!passwordEncoder.matches(request.currentPassword, user.passwordHash)){
            throw ResourceNotFoundException("Contraseña actual invalida")
        }

        user.passwordHash = passwordEncoder.encode(request.newPassword)!!
        userRepository.save(user)
    }

    private fun toResponse(user: User): UserResponse {
        return UserResponse(
            id = requireNotNull(user.id),
            firstName = user.firstName,
            lastName = user.lastName,
            username = user.username,
            email = user.email,
            role = user.role,
            active = user.active
        )
    }
}

