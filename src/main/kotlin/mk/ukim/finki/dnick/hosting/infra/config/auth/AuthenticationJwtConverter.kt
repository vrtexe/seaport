package mk.ukim.finki.dnick.hosting.infra.config.auth

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

/**
 * Converter from JWT tokens to our custom Authentication object.
 */
@Component
class AuthenticationJwtConverter(private val pidsUserFactory: UserFactory) :
    Converter<Jwt, AbstractAuthenticationToken> {
    /**
     * Creates our custom Authentication object from the given JWT token.
     * Sets the granted permissions from the user permissions based on their roles.
     * Adds the roles with the ROLE_ prefix as permissions so the spring's
     * default hasAuthority and hasRole work with our roles and permissions.
     *
     * @param source JWT token.
     * @return the PidsAuthenticationToken (our custom Authentication object).
     */
    override fun convert(source: Jwt): AbstractAuthenticationToken {
        val user: User = pidsUserFactory.buildFromJwt(source)

        val permissions: Set<GrantedAuthority> = user.permissions
            .map { s -> SimpleGrantedAuthority(s.value()) }
            .toSet()

        val userRoles: Set<GrantedAuthority> = user.userRoles
            .map { s -> SimpleGrantedAuthority("${ROLE_AUTHORITY_PREFIX}_${s.value()}") }
            .toSet()

        return AuthenticationToken(source, permissions union userRoles, user)
    }

    companion object {
        private const val ROLE_AUTHORITY_PREFIX = "ROLE"
    }
}