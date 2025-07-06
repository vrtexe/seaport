package mk.ukim.finki.dnick.hosting.infra.config.auth

interface Permission {
    fun value(): String?
}