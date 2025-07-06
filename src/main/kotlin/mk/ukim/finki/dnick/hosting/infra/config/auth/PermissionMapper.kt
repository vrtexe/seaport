package mk.ukim.finki.dnick.hosting.infra.config.auth

interface PermissionMapper {

    fun getPermissions(userRole: UserRole?): List<Permission>

    fun parsePermissions(permissions: String): Permission?
}