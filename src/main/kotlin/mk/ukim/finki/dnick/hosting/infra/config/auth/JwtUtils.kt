package mk.ukim.finki.dnick.hosting.infra.config.auth

import org.springframework.security.oauth2.jwt.Jwt

/**
 * Utility functions for extracting information from JWT tokens.
 */
object JwtUtils {
    private const val REALM_ACCESS_CLAIM = "realm_access"
    private const val RESOURCE_ACCESS_CLAIM = "resource_access"
    private const val ROLES_CLAIM = "roles"
    private const val AZP_CLAIM = "azp" // Authorized Party which for keycloak has the client used
    private const val USERNAME_CLAIM = "preferred_username"
    private const val EMAIL_CLAIM = "email"
    private const val ORGANIZATION_CLAIM = "organization"
    private const val LOCALE_CLAIM = "locale"
    private const val FIRST_NAME_CLAIM = "given_name"
    private const val LAST_NAME_CLAIM = "family_name"

    /**
     * Extracts the username from the JWT token.
     *
     * @param jwt the JWT token provided for accessing a resource
     * @return the username of the user who the JWT token belongs to
     */
    fun getUsername(jwt: Jwt): String {
        return jwt.getClaimAsString(USERNAME_CLAIM)
    }

    /**
     * Extracts the email from the JWT token.
     *
     * @param jwt the JWT token provided for accessing a resource
     * @return the email of the user who the JWT token belongs to
     */
    fun getEmail(jwt: Jwt): String {
        return jwt.getClaimAsString(EMAIL_CLAIM)
    }

    /**
     * Extracts the organization attribute of the user from the JWT token.
     *
     * @param jwt the JWT token provided for accessing a resource
     * @return the organization attribute of the user who the JWT token belongs to
     */
    fun getOrganization(jwt: Jwt): String {
        return jwt.getClaimAsString(ORGANIZATION_CLAIM)
    }

    /**
     * Extracts the locale attribute of the user from the JWT token.
     *
     * @param jwt the JWT token provided for accessing a resource
     * @return the locale attribute of the user who the JWT token belongs to
     */
    fun getLocale(jwt: Jwt): String? {
        return jwt.getClaimAsString(LOCALE_CLAIM)
    }

    /**
     * Extracts the subject as id from the JWT token.
     *
     * @param jwt the JWT token provided for accessing a resource
     * @return the subject field of the token.
     */
    fun getId(jwt: Jwt): String {
        return jwt.subject
    }

    /**
     * Extracts the first name attribute of the user from the JWT token.
     *
     * @param jwt the JWT token provided for accessing a resource
     * @return the first name attribute of the user who the JWT token belongs to
     */
    fun getFirstName(jwt: Jwt): String {
        return jwt.getClaimAsString(FIRST_NAME_CLAIM)
    }

    /**
     * Extracts the last name attribute of the user from the JWT token.
     *
     * @param jwt the JWT token provided for accessing a resource
     * @return the last name attribute of the user who the JWT token belongs to
     */
    fun getLastName(jwt: Jwt): String {
        return jwt.getClaimAsString(LAST_NAME_CLAIM)
    }

    /**
     * Returns the JWT token in string format.
     *
     * @param jwt the JWT token provided for accessing a resource
     * @return the JWT token in string format.
     */
    fun getToken(jwt: Jwt): String {
        return jwt.tokenValue
    }

    /**
     * Extracts the user roles from the JWT token. The Keycloak default
     * realm_access claim is used.
     *
     * @param jwt the JWT token provided for accessing a resource
     * @return the roles of the user who the JWT token belongs to
     */
    fun getRealmRoles(jwt: Jwt): Set<String> {
        val realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM) ?: return setOf()
        val rolesObject = realmAccess[ROLES_CLAIM]

        if (rolesObject !is Collection<*>) {
            return setOf()
        }

        return rolesObject.filterNotNull()
            .map { it.toString() }
            .toSet()
    }

    fun getClientPermissions(jwt: Jwt): Set<String> {
        val realmAccess = jwt.getClaimAsMap(RESOURCE_ACCESS_CLAIM) ?: return setOf()

        return realmAccess.values.asSequence()
            .filterIsInstance<Map<*, *>>()
            .map { it[ROLES_CLAIM] }
            .filterIsInstance<Collection<*>>()
            .flatten()
            .filterNotNull()
            .map { it.toString() }
            .toSet()
    }

}