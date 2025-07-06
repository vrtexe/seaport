package mk.ukim.finki.dnick.hosting.infra.config.auth

enum class UserRole(private val value: String) {
    ADMIN("ADMIN"),
    USER("USER");

    fun value(): String {
        return value
    }

    companion object {
        @JvmStatic
        private val VALUE_MAP: Map<String, UserRole> = entries.associateBy { it.value() }

        @JvmStatic
        fun of(value: String): UserRole? {
            return VALUE_MAP[value]
        }
    }
}