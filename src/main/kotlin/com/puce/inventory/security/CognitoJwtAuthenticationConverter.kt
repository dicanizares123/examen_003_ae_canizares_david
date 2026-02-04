package com.puce.inventory.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

/**
 * Custom JWT converter that extracts roles from Cognito groups claim
 * and converts them to Spring Security authorities
 */
@Component
class CognitoJwtAuthenticationConverter(
    @Value("\${app.security.roles-claim:cognito:groups}")
    private val rolesClaim: String
) : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val authorities = extractAuthorities(jwt)
        return JwtAuthenticationToken(jwt, authorities, jwt.subject)
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>()

        // Extract roles from the configured claim (default: cognito:groups)
        val groups = try {
            jwt.getClaimAsStringList(rolesClaim) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        // Convert groups to ROLE_ authorities
        groups.forEach { group ->
            authorities.add(SimpleGrantedAuthority("ROLE_$group"))
        }

        // Also add the scope-based authorities if present
        val scopes = try {
            jwt.getClaimAsString("scope")?.split(" ") ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        scopes.forEach { scope ->
            authorities.add(SimpleGrantedAuthority("SCOPE_$scope"))
        }

        return authorities
    }
}

