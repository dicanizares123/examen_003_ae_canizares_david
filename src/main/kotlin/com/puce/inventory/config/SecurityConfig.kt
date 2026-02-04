package com.puce.inventory.config

import com.puce.inventory.security.CognitoJwtAuthenticationConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val cognitoJwtAuthenticationConverter: CognitoJwtAuthenticationConverter,

    @Value("\${app.security.admin-role:ADMIN}")
    private val adminRole: String
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
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

                    // Admin-only endpoints - require ADMIN role
                    .requestMatchers(HttpMethod.POST, "/api/rules/**").hasRole(adminRole)
                    .requestMatchers(HttpMethod.PUT, "/api/rules/**").hasRole(adminRole)
                    .requestMatchers(HttpMethod.PATCH, "/api/rules/**").hasRole(adminRole)
                    .requestMatchers(HttpMethod.DELETE, "/api/rules/**").hasRole(adminRole)

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

