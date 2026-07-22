package co.edu.iub.manaproject.config

import co.edu.iub.manaproject.model.User
import co.edu.iub.manaproject.model.UserRole
import co.edu.iub.manaproject.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
): CommandLineRunner {

    override fun run(vararg args: String) {
        if (!userRepository.existsByEmail("daniel@gmail.com")) {
            val admin = User(
                firstName = "Daniel",
                lastName = "Henriquez",
                username = "Daniel",
                email = "daniel@gmail.com",
                passwordHash = passwordEncoder.encode("Daniel123.")!!,
                role = UserRole.ADMIN
            )
            userRepository.save(admin)
        }
    }

}