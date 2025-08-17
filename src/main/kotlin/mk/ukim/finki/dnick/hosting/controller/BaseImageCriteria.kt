package mk.ukim.finki.dnick.hosting.controller

import mk.ukim.finki.dnick.hosting.generated.model.BaseImageType

data class BaseImageCriteria(
    val type: BaseImageType?,
    val language: String?,
    val buildTool: String?
)
