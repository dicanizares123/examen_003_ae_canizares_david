package com.puce.inventory.config

import com.puce.inventory.security.CognitoJwtAuthenticationConverter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import jakarta.annotation.PostConstruct

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val cognitoJwtAuthenticationConverter: CognitoJwtAuthenticationConverter,

    @Value("\${app.security.admin-role:ADMIN}")
    private val adminRole: String
) {

    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    @PostConstruct
    fun init() {
        logger.info("SecurityConfig initialized with adminRole: '$adminRole'")
        logger.info("Security will check for ROLE_$adminRole")
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        logger.info("Configuring security filter chain with adminRole: $adminRole")

        http
            // Disable CSRF for REST API
            .csrf { it.disable() }

            // Stateless session management
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            // Configure authorization rules
            .authorizeHttpRequests { authorize ->
                authorize
                    // Public endpoints - no authentication required
                    .requestMatchers("/public/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/error").permitAll()

                    // Admin-only endpoints - require admin role
                    // Using hasAuthority with explicit ROLE_ prefix for clarity
                    .requestMatchers(HttpMethod.POST, "/api/rules").hasAuthority("ROLE_$adminRole")
                    .requestMatchers(HttpMethod.POST, "/api/rules/**").hasAuthority("ROLE_$adminRole")
                    .requestMatchers(HttpMethod.PUT, "/api/rules/**").hasAuthority("ROLE_$adminRole")
                    .requestMatchers(HttpMethod.PATCH, "/api/rules/**").hasAuthority("ROLE_$adminRole")
                    .requestMatchers(HttpMethod.DELETE, "/api/rules/**").hasAuthority("ROLE_$adminRole")

                    // Authenticated endpoints - require valid JWT (any role)
                    .requestMatchers(HttpMethod.GET, "/api/**").authenticated()

                    // All other requests require authentication
                    .anyRequest().authenticated()
            }

            // Configure OAuth2 Resource Server with JWT
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(cognitoJwtAuthenticationConverter)
                }
            }

        return http.build()
    }
}

