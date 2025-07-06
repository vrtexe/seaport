package mk.ukim.finki.dnick.hosting.infra.config.auth

import java.util.*

/**
 * Custom user object.
 */
data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val locale: Locale,
    val userRoles: Set<UserRole>,
    val permissions: Set<Permission>,
    val username: String,
    val token: String?
) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        return this.id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

}
