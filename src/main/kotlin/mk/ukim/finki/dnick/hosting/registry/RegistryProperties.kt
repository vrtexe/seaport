package mk.ukim.finki.dnick.hosting.registry

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app-host.registry")
data class RegistryProperties(val url: String)
