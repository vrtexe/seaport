package mk.ukim.finki.dnick.hosting.infra.config.auth

import com.nimbusds.jwt.JWT
import mk.ukim.finki.dnick.hosting.infra.config.auth.JwtUtils.getClientPermissions
import mk.ukim.finki.dnick.hosting.infra.config.auth.JwtUtils.getEmail
import mk.ukim.finki.dnick.hosting.infra.config.auth.JwtUtils.getFirstName
import mk.ukim.finki.dnick.hosting.infra.config.auth.JwtUtils.getId
import mk.ukim.finki.dnick.hosting.infra.config.auth.JwtUtils.getLastName
import mk.ukim.finki.dnick.hosting.infra.config.auth.JwtUtils.getLocale
import mk.ukim.finki.dnick.hosting.infra.config.auth.JwtUtils.getRealmRoles
import mk.ukim.finki.dnick.hosting.infra.config.auth.JwtUtils.getToken
import mk.ukim.finki.dnick.hosting.infra.config.auth.JwtUtils.getUsername
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import java.util.*


/**
 * Custom user object builder methods.
 */
@Component
class UserFactory(
    @Autowired(required = false) private val rolePermissionsMapper: PermissionMapper?
) {
    //  private final PidsRolePermissionMapper rolePermissionsMapper;
    //  private final PidsOrganizationRoleMapper organizationRolesMapper;
    //  public UserFactory(PidsRolePermissionMapper rolePermissionsMapper,
    //                     PidsOrganizationRoleMapper organizationRolesMapper) {
    //    this.rolePermissionsMapper = rolePermissionsMapper;
    //    this.organizationRolesMapper = organizationRolesMapper;
    //  }
    /**
     * Creates a custom user object from the attributes of the given JWT token.
     *
     * @param source JWT token.
     * @return user object.
     */
    fun buildFromJwt(source: Jwt): User {
        val userRoles = convertRoles(getRealmRoles(source))

        return User(
            id = getId(source),
            firstName = getFirstName(source),
            lastName = getLastName(source),
            email = getEmail(source),
            locale = convertLocale(getLocale(source)) ?: Locale.ENGLISH,
            userRoles = userRoles,
            permissions = extractPermissions(source, userRoles),
            username = getUsername(source),
            token = getToken(source),
        )
    }

    private fun convertLocale(locale: String?): Locale {
        return locale
            ?.let { Locale.forLanguageTag(locale.lowercase(Locale.getDefault())) }
            ?: Locale.ENGLISH
    }

    private fun convertRoles(realmRoles: Set<String>): Set<UserRole> {
        return realmRoles.mapNotNull(UserRole::of).toSet()
    }

    private fun extractPermissions(jwt: Jwt, userRoles: Set<UserRole>): Set<Permission> {
        return getUserPermissions(userRoles) union extractPermissions(jwt)
    }

    private fun getUserPermissions(userRoles: Set<UserRole>): Set<Permission> {
        return userRoles
            .map { userRole -> rolePermissionsMapper?.getPermissions(userRole) ?: listOf() }
            .flatten()
            .toSet()
    }

    private fun extractPermissions(jwt: Jwt): Set<Permission> {
        return getClientPermissions(jwt)
            .mapNotNull { rolePermissionsMapper?.parsePermissions(it) }
            .toSet()
    }
}