package mk.ukim.finki.dnick.hosting.model.dto

import mk.ukim.finki.dnick.hosting.image.ImageParamsTyped

data class ImageBuildRequestDto(
    val imageId: Int,
    val request: ImageParamsTyped,
    val socket: String?
)
