package mk.ukim.finki.dnick.hosting.infra.config.auth

import org.springframework.stereotype.Component

@Component
class DefaultPermissionMapper : PermissionMapper {

    override fun getPermissions(userRole: UserRole?): List<Permission> {
        return listOf(AppPermission.DEFAULT)
    }

    override fun parsePermissions(permissions: String): Permission? {
        return AppPermission.of(permissions)
    }
}