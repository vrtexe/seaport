package mk.ukim.finki.dnick.hosting.model.domain

data class BaseImageArg(
    val id: Int,
    val name: String,
    val description: String,
    val type: BaseImageArgType = BaseImageArgType.STRING,
    val stage: BaseImageArgStage = BaseImageArgStage.BUILD
)

enum class BaseImageArgType {
    STRING, FILE
}

enum class BaseImageArgStage {
    RUNTIME, BUILD
}