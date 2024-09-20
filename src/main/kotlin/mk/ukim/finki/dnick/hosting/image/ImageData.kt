package mk.ukim.finki.dnick.hosting.image

import org.springframework.web.multipart.MultipartFile

data class ImageBaseParams(
    val language: String,
    val version: String? = null
)

data class ImageData(
    val name: String,
    val version: String
)

data class ImagesExeRequest(
    val namespace: String,
    val images: Map<String, ImageExeRequest>
)

data class ImageExeRequest(
    val uid: String,
    val data: ImageData,
    val base: ImageBaseParams,
    val buildArgs: Map<String, String> = mapOf()
)

data class ImageExeParams(
    val namespace: String,
    val file: MultipartFile,
    val base: ImageBaseParams,
    override val uid: String,
    override val data: ImageData,
    override val buildArgs: Map<String, String> = mapOf(),
) : ImageParamsTyped(ImageParamsType.EXE)

data class ImageGitParams(
    val namespace: String,
    val buildTool: String,
    val version: String,
    val base: ImageBaseParams,
    override val uid: String,
    override val data: ImageData,
    override val buildArgs: Map<String, String> = mapOf(),
) : ImageParamsTyped(ImageParamsType.GIT)

abstract class ImageParamsTyped(
    val type: ImageParamsType
) {
    abstract val uid: String
    abstract val data: ImageData
    abstract val buildArgs: Map<String, String>
}

enum class ImageParamsType {
    GIT, EXE
}