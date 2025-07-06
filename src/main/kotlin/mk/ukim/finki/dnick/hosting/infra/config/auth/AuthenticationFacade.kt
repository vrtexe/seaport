package mk.ukim.finki.dnick.hosting.infra.config.auth

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthenticationFacade {

    val user: User? get() = authentication?.user

    private val authentication: AuthenticationToken?
        get() {
            val authentication = SecurityContextHolder.getContext().authentication

            if (authentication !is AuthenticationToken) {
                return null
            }

            return authentication
        }
}
