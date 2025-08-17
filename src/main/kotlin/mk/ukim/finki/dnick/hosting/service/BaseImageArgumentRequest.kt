package mk.ukim.finki.dnick.hosting.service

import mk.ukim.finki.dnick.hosting.model.entity.BaseImageType

data class BaseImageArgumentRequest(
    val type: BaseImageType,
    val language: String,
    val languageVersion: String?,
    val buildTool: BuildToolRequest?
)

data class BuildToolRequest(
    val name: String,
    val version: String,
)