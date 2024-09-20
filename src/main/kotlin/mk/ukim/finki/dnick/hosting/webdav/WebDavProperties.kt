package mk.ukim.finki.dnick.hosting.webdav

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app-host.webdav")
data class WebDavProperties(
    val baseUrl: String,
    val k8sUrl: String,
)
