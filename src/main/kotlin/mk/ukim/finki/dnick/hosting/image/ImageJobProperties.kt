package mk.ukim.finki.dnick.hosting.image

data class ImageJobProperties(
    val name: String,
    val version: String,
    val key: String = "$name-$version",
    val content: String,
    val buildArguments: Map<String, String>,
)