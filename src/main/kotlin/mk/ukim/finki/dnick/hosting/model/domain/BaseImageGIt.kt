package mk.ukim.finki.dnick.hosting.model.domain


data class BaseImageGit(
    val id: Int,
    val buildTool: String,
    val version: String,
    val value: String,

    val base: BaseImage,
    val refId: Int,

    val arguments: Set<BaseImageArg>
) : BaseImageTyped(BaseImageType.GIT)