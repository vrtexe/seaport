package mk.ukim.finki.dnick.hosting.model.domain

enum class BaseImageType {
    GIT, EXE
}

abstract class BaseImageTyped(val type: BaseImageType)
