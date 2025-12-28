package mk.ukim.finki.dnick.hosting.mapper

import mk.ukim.finki.dnick.hosting.generated.model.*
import mk.ukim.finki.dnick.hosting.generated.model.BaseImage
import mk.ukim.finki.dnick.hosting.model.entity.*
import mk.ukim.finki.dnick.hosting.model.entity.BaseImageType
import mk.ukim.finki.dnick.hosting.model.entity.Image
import mk.ukim.finki.dnick.hosting.model.entity.ImageTag
import mk.ukim.finki.dnick.hosting.model.entity.User
import mk.ukim.finki.dnick.hosting.generated.model.User as UserDto
import mk.ukim.finki.dnick.hosting.service.InternalArgument
import java.time.ZoneId
import mk.ukim.finki.dnick.hosting.generated.model.BaseImageType.Companion as BaseImageTypeDto
import mk.ukim.finki.dnick.hosting.generated.model.Image as ImageDto
import mk.ukim.finki.dnick.hosting.generated.model.ImageTag as ImageTagDto


fun Image.toDto() = ImageDto(
    id = this.id!!,
    name = this.name,
    user = this.namespace.let {
        it.user.let { user ->
            UserDto(
                uid = user.uid.toString(),
                username = user.username,
                firstName = user.firstName,
                lastName = user.lastName,
                namespace = it.name
            )
        }
    },
    latest = this.tags.maxByOrNull { it.createdAt }?.toDto(),
    releases = this.tags.size
)

fun ImageTag.toDto() = ImageTagDto(
    id = this.id!!,
    uid = this.hash.toString(),
    version = this.version,
    created = this.createdAt.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
    status = this.status.toDto()
)

fun ImageStatus.toDto() = ImageTagStatus.forValue(this.name.uppercase())

fun Image.toDetailsDto() = ImageDetails(
    id = this.id!!,
    name = this.name,
    user = this.namespace.let {
        it.user.let { user ->
            UserDto(
                uid = user.uid.toString(),
                username = user.username,
                firstName = user.firstName,
                lastName = user.lastName,
                namespace = it.name
            )
        }
    },
    tags = this.tags.map { it.toDetailsDto() }.sortedByDescending { it.created },
    releases = this.tags.size
)

fun ImageTag.toDetailsDto() = ImageTagDetails(
    id = this.id!!,
    version = this.version,
    created = this.createdAt.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
    arguments = this.arguments
        .filter {
            !InternalArgument.valuesByType.getOrElse(this.base.type) { listOf() }
                .contains(it.key)
        },
    uid = this.hash.toString(),
    status = this.status.toDto(),
    base = this.base.toDetailsDto()
)

fun BaseImageRef.toDetailsDto() = BaseImage(
    id = this.id!!,
    type = this.type.toDetailsDto(),
    language = when (this.type) {
        BaseImageType.GIT -> this.baseImageGit?.base?.language ?: ""
        BaseImageType.EXE -> this.baseImageExe?.base?.language ?: ""
    },
    version = when (this.type) {
        BaseImageType.GIT -> this.baseImageGit?.base?.version ?: ""
        BaseImageType.EXE -> this.baseImageExe?.base?.version ?: ""
    },
    git = this.baseImageGit?.let {
        BaseImageRequestGit(
            buildTool = it.buildTool,
            buildToolVersion = it.version
        )
    },
    arguments = this.arguments.map {
        BaseImageArgument(
            name = it.name,
            description = it.description,
            type = it.type.toDetailsDto(),
            stage = it.stage.toDetailsDto()
        )
    }
)

fun BaseImageType.toDetailsDto() = BaseImageTypeDto.forValue(this.name.uppercase())
fun BaseImageArgType.toDetailsDto() = BaseArgumentType.forValue(this.name.uppercase())
fun BaseImageArgStage.toDetailsDto() = BaseArgumentStage.forValue(this.name.uppercase())

