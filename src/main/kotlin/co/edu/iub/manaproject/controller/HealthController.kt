package co.edu.iub.manaproject.controller

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/")
class HealthController(private val jdbcTemplate: JdbcTemplate) {

    @GetMapping
    fun check(): Map<String, String> {
        jdbcTemplate.queryForObject("Select 1", Int::class.java)
    return mapOf(
        "status" to "UP",
        "database" to "CONNECTED"
    )

    }

}