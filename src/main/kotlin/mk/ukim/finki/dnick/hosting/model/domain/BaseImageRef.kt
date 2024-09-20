package mk.ukim.finki.dnick.hosting.model.domain

data class BaseImageRef(
    val id: Int,
    val arguments: Set<BaseImageArg> = mutableSetOf()
)