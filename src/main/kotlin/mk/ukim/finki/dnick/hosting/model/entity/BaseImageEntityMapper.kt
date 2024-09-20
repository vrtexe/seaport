package mk.ukim.finki.dnick.hosting.model.entity

import mk.ukim.finki.dnick.hosting.model.domain.BaseImage as DomainBaseImage
import mk.ukim.finki.dnick.hosting.model.domain.BaseImageArg as DomainBaseImageArg
import mk.ukim.finki.dnick.hosting.model.domain.BaseImageArgStage as DomainBaseImageArgStage
import mk.ukim.finki.dnick.hosting.model.domain.BaseImageArgType as DomainBaseImageArgType
import mk.ukim.finki.dnick.hosting.model.domain.BaseImageExe as DomainBaseImageExe
import mk.ukim.finki.dnick.hosting.model.domain.BaseImageGit as DomainBaseImageGit


fun BaseImageGit.toDomain() = DomainBaseImageGit(
    id = this.id!!,
    buildTool = this.buildTool,
    version = this.version,
    refId = this.ref.id!!,
    value = this.value,
    base = this.base.toDomain(),
    arguments = this.ref.arguments.map { it.toDomain() }.toSet()
)


fun BaseImageExe.toDomain() = DomainBaseImageExe(
    id = this.id!!,
    value = this.value,
    base = this.base.toDomain(),
    refId = this.ref.id!!,
    arguments = this.ref.arguments.map { it.toDomain() }.toSet()
)

fun BaseImageArg.toDomain() = DomainBaseImageArg(
    id = this.id!!,
    name = this.name,
    description = this.description,
    type = this.type.toDomain(),
    stage = this.stage.toDomain()
)

fun BaseImageArgType.toDomain() = DomainBaseImageArgType.valueOf(this.name.uppercase())

fun BaseImageArgStage.toDomain() = DomainBaseImageArgStage.valueOf(this.name.uppercase())

fun BaseImage.toDomain() = DomainBaseImage(
    id = this.id!!,
    language = this.language,
    version = this.version ?: "default"
)

