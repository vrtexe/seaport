package mk.ukim.finki.dnick.hosting.infra.config

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "app-host.k8s.config")
data class KubernetesProperties(val data: String, val format: KubernetesConfigFormat)

enum class KubernetesConfigFormat {
    CLASSPATH,
    STRING,
    FILE
}