package mk.ukim.finki.dnick.hosting.model.domain

data class BaseImageExe(
    val id: Int,
    val value: String,

    val base: BaseImage,
    val refId: Int,

    val arguments: Set<BaseImageArg>
) : BaseImageTyped(BaseImageType.EXE)