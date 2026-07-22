package co.edu.iub.manaproject.config

import co.edu.iub.manaproject.repository.UserRepository
import co.edu.iub.manaproject.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = header.removePrefix("Bearer ").trim()

        try {
            val username = jwtService.extractUsername(token)

            if (
                jwtService.isTokenValid(token) &&
                SecurityContextHolder.getContext().authentication == null
            ) {
                val user = userRepository.findByUsername(username)

                if (user != null && user.active) {
                    val authorities = listOf(
                        SimpleGrantedAuthority("ROLE_${user.role.name}")
                    )

                    val authentication = UsernamePasswordAuthenticationToken(
                        user.username,
                        null,
                        authorities
                    )

                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (_: Exception) {

        }

        filterChain.doFilter(request, response)
    }
}