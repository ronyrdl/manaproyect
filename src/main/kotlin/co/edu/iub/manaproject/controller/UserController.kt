package co.edu.iub.manaproject.controller

import co.edu.iub.manaproject.dto.MessageResponse
import co.edu.iub.manaproject.dto.user.ChangePasswordRequest
import co.edu.iub.manaproject.dto.user.UpdateProfileRequest
import co.edu.iub.manaproject.dto.user.UpdateUserRoleRequest
import co.edu.iub.manaproject.dto.user.UserResponse
import co.edu.iub.manaproject.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/users")
class UserController(
    val userService: UserService
) {
    @GetMapping
    fun list(): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(userService.getAllUsers())
    }

    @GetMapping("/me")
    fun getProfile(authentication: Authentication): UserResponse {
        return userService.getProfile(authentication.name)
    }

    @PutMapping("/me")
    fun updateProfile(
        @Valid @RequestBody request: UpdateProfileRequest,
        authentication: Authentication
    ): UserResponse {
        return userService.updateProfile(authentication.name, request)
    }

    @PutMapping("/{id}/role")
    fun updateRole(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRoleRequest
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.updateRole(id, request))
    }

    @PostMapping("/me/change-password")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest,
        authentication: Authentication
    ): ResponseEntity<MessageResponse> {
        userService.changePassword(authentication.name, request)
        return ResponseEntity.ok(MessageResponse("Password changed successfully"))
    }
}