package co.edu.iub.manaproject.repository

import co.edu.iub.manaproject.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {

    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
    fun findByUsernameOrEmail(username: String, email: String): User?
    fun findByUsername(username: String): User?
    fun existsByEmailAndIdNot(email: String, id: Long): Boolean
}