package mk.ukim.finki.dnick.hosting.image

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
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

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(value = ImageGitParams::class, name = ImageParamsType.GIT_NAME),
        JsonSubTypes.Type(value = ImageExeParams::class, name = ImageParamsType.EXE_NAME)
    ]
)
abstract class ImageParamsTyped(
    val type: ImageParamsType
) {
    abstract val uid: String
    abstract val data: ImageData
    abstract val buildArgs: Map<String, String>
}

enum class ImageParamsType {
    GIT, EXE;

    companion object {
        const val GIT_NAME = "GIT"
        const val EXE_NAME = "EXE"
    }
}