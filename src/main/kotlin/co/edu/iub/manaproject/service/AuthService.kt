package co.edu.iub.manaproject.service

import co.edu.iub.manaproject.dto.auth.LoginRequest
import co.edu.iub.manaproject.dto.auth.TokenResponse
import co.edu.iub.manaproject.dto.user.CreateUserRequest
import co.edu.iub.manaproject.dto.user.UserResponse

import co.edu.iub.manaproject.exception.DuplicateResourceException
import co.edu.iub.manaproject.exception.InvalidCredentialsException
import co.edu.iub.manaproject.model.User
import co.edu.iub.manaproject.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {

    fun createUser(request: CreateUserRequest): UserResponse {
        val email = request.email.trim().lowercase()
        val username = request.username.trim().lowercase()

    if (userRepository.existsByUsername(username)) {
        throw DuplicateResourceException("El nombre de usuario ya existe")
    }
        if (userRepository.existsByEmail(email)) {
            throw DuplicateResourceException("El correo ya está registrado")
        }
        val user = User(
            firstName = request.firstName.trim(),
            lastName = request.lastName.trim(),
            username = username,
            email = email,
            passwordHash = passwordEncoder.encode(request.password)!!
        )
    return toResponse(userRepository.save(user))
    }

    fun login(request: LoginRequest): TokenResponse {
        val identifier = request.identifier.trim().lowercase()
        val user = userRepository.findByUsernameOrEmail(identifier, identifier)

            ?: throw DuplicateResourceException("Credenciales invalidas")

        if(!user.active || !passwordEncoder.matches(request.password, user.passwordHash)){
            throw InvalidCredentialsException("Credenciales invalidas")
        }

        val token = jwtService.generateToken(user)
        return TokenResponse(
            accessToken = token,
            expiresIn = jwtService.expirationMinutes * 60
        )

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