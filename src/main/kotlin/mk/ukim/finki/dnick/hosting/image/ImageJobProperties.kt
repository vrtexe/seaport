package mk.ukim.finki.dnick.hosting.image

import java.util.UUID

data class ImageJobProperties(
    val imageUid: UUID,
    val name: String,
    val version: String,
    val key: String = "$name-$version",
    val content: String,
    val buildArguments: Map<String, String>,
)