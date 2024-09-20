package mk.ukim.finki.dnick.hosting.image

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app-host.image-builder")
data class ImageBuilderProperties(
    val registry: String = "10.96.101.236",
    val podName: String = "kaniko-pod",
    val jobPrefix: String = "image-builder",
    val namespace: String = "registry-internal",
    val labels: ImageBuilderLabels = ImageBuilderLabels()
)

data class ImageBuilderLabels(
    val app: ImageBuilderLabel = ImageBuilderLabel("app", "image-builder"),
    val image: ImageBuilderLabel = ImageBuilderLabel("image")
)

data class ImageBuilderLabel(
    val name: String = "",
    val value: String = ""
) {

    fun toPair(): Pair<String, String> {
        return Pair(name, value)
    }

    fun toSelector(): String {
        return "$name=$value"
    }
}

