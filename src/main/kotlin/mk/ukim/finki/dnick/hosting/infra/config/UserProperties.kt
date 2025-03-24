package mk.ukim.finki.dnick.hosting.infra.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app-host.user")
data class UserProperties(
    val namespace: String
)
