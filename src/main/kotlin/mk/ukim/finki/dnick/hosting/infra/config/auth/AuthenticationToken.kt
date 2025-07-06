package mk.ukim.finki.dnick.hosting.infra.config.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

data class AuthenticationToken(
    val jwt: Jwt,
    val authorities: Set<GrantedAuthority>,
    val user: User
) : JwtAuthenticationToken(jwt, authorities)
