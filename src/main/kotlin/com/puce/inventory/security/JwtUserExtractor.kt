package com.puce.inventory.security

import com.puce.inventory.exception.UserIdNotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class JwtUserExtractor(
    @Value("\${app.security.user-id-claim:sub}")
    private val userIdClaim: String,

    @Value("\${app.security.roles-claim:cognito:groups}")
    private val rolesClaim: String,

    @Value("\${app.security.admin-role:ADMIN}")
    private val adminRole: String
) {

    /**
     * Extracts the user ID from the JWT token (using the configured claim, default: 'sub')
     * @throws UserIdNotFoundException if the user ID is not found in the token
     */
    fun extractUserId(): String {
        val jwt = getJwt()
        return jwt.getClaimAsString(userIdClaim)
            ?: throw UserIdNotFoundException("User ID claim '$userIdClaim' not found in JWT token")
    }

    /**
     * Extracts roles/groups from the JWT token
     */
    fun extractRoles(): List<String> {
        val jwt = getJwt()
        return try {
            jwt.getClaimAsStringList(rolesClaim) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Checks if the current user has the ADMIN role
     */
    fun isAdmin(): Boolean {
        return extractRoles().any { it.equals(adminRole, ignoreCase = true) }
    }

    /**
     * Gets the current username/email from the token
     */
    fun getUsername(): String? {
        val jwt = getJwt()
        return jwt.getClaimAsString("username")
            ?: jwt.getClaimAsString("email")
            ?: jwt.getClaimAsString("preferred_username")
    }

    private fun getJwt(): Jwt {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw UserIdNotFoundException("No authentication found in security context")

        val principal = authentication.principal
        if (principal is Jwt) {
            return principal
        }
        throw UserIdNotFoundException("Authentication principal is not a JWT token")
    }
}
