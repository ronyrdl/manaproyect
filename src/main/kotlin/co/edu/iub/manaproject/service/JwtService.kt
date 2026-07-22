package co.edu.iub.manaproject.service

import co.edu.iub.manaproject.model.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @param:Value("\${app.jwt.secret}") private val secret: String,
    @param:Value("\${app.jwt.expiration-minutes}") val expirationMinutes: Long
) {

    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))
    }

    fun generateToken(user: User): String {
        val now = Date()
        val expiration = Date(now.time + expirationMinutes * 60_000)

        return Jwts.builder()
            .subject(user.username)
            .claim("role", user.role.name)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(signingKey)
            .compact()
    }

    fun extractEmail(token: String): String? {
        return extractAllClaims(token)?.subject
    }

    fun extractUsername(token: String): String {
        return extractClaims(token).subject
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            extractAllClaims(token) != null && !isTokenExpired(token)
        } catch (ex: Exception) {
            false
        }
    }

    private fun isTokenExpired(token: String): Boolean {
        val expiration = extractAllClaims(token)?.expiration ?: return true
        return expiration.before(Date())
    }

    private fun extractAllClaims(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (ex: Exception) {
            null
        }
    }

    private fun extractClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}