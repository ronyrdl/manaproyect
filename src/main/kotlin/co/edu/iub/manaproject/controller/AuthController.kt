package co.edu.iub.manaproject.controller

import co.edu.iub.manaproject.dto.auth.LoginRequest
import co.edu.iub.manaproject.dto.auth.TokenResponse
import co.edu.iub.manaproject.dto.user.CreateUserRequest
import co.edu.iub.manaproject.dto.user.UserResponse
import co.edu.iub.manaproject.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(authService.createUser(request))
    }
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(authService.login(request))
    }
}