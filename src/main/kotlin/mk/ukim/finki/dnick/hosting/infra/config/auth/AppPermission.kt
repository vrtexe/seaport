package mk.ukim.finki.dnick.hosting.infra.config.auth

enum class AppPermission(private val value: String) : Permission {
    DEFAULT("DEFAULT"),
    IMAGE_READ("image-read"),
    IMAGE_WRITE("image-write");


    override fun value(): String {
        return value
    }

    companion object {
        @JvmStatic
        private val VALUE_MAP: Map<String, AppPermission> = entries.associateBy { it.value() }

        @JvmStatic
        fun of(value: String): AppPermission? {
            return VALUE_MAP[value]
        }
    }
}