package co.edu.iub.manaproject.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
private val jwtAuthenticationFilter: JwtAuthenticationFilter
){

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    return http
        .csrf { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests {
            it.requestMatchers("/").permitAll()
            it.requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
            it.requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
            it.requestMatchers(HttpMethod.GET, "/menu/**").permitAll()
            it.requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
            it.requestMatchers("/error").permitAll()
            it.requestMatchers(
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**"
            ).permitAll()
            it.requestMatchers(HttpMethod.POST, "/categories/**").hasRole("ADMIN")
            it.requestMatchers(HttpMethod.PUT, "/categories/**").hasRole("ADMIN")
            it.requestMatchers(HttpMethod.PATCH, "/categories/**").hasRole("ADMIN")
            it.requestMatchers(HttpMethod.DELETE, "/categories/**").hasRole("ADMIN")
            it.requestMatchers(HttpMethod.GET, "/products/**").hasRole("ADMIN")
            it.requestMatchers(HttpMethod.POST, "/products/**").hasRole("ADMIN")
            it.requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMIN")
            it.requestMatchers(HttpMethod.PATCH, "/products/**").hasRole("ADMIN")
            it.requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN")
            it.requestMatchers(HttpMethod.GET, "/users/**").hasRole("ADMIN")
            it.requestMatchers("/orders/my-orders/**").authenticated()
            it.requestMatchers(HttpMethod.GET, "/orders/**").hasRole("ADMIN")
            it.requestMatchers(HttpMethod.POST, "/orders/**").authenticated()
            it.requestMatchers(HttpMethod.PATCH, "/orders/**").hasRole("ADMIN")
            it.requestMatchers(HttpMethod.DELETE, "/orders/**").hasRole("ADMIN")
            it.requestMatchers("/cart/**").authenticated()
            it.requestMatchers("/stats/**").hasRole("ADMIN")
            it.requestMatchers("/audit/**").hasRole("ADMIN")
            it.requestMatchers("/config/**").hasRole("ADMIN")
            it.anyRequest().authenticated()
        }
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java
        )
        .formLogin { it.disable() }
        .httpBasic { it.disable() }
        .build()
    }
}