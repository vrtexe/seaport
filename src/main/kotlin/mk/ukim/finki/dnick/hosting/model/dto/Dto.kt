package mk.ukim.finki.dnick.hosting.model.dto

import mk.ukim.finki.dnick.hosting.model.domain.Application
import mk.ukim.finki.dnick.hosting.model.domain.ExternalService
import mk.ukim.finki.dnick.hosting.model.domain.Image
import mk.ukim.finki.dnick.hosting.model.domain.Namespace

data class NamespaceDto(
    val uid: String,
    val name: String,
    val applications: Set<ApplicationDto>,
    val externalService: Set<ExternalServiceDto>
)

data class ExternalServiceDto(val id: Int)


data class ApplicationDto(
    val id: Int,
    val name: String,
    val images: Set<ImageDto>
)

data class ImageDto(
    val id: Int,
    val name: String,
    var version: String,
)

fun Namespace.toDto() = NamespaceDto(
    uid = this.id.toString(),
    name = this.name,
    applications = this.applications.map { it.toDto() }.toSet(),
    externalService = this.externalServices.map { it.toDto() }.toSet(),
)

fun ExternalService.toDto() = ExternalServiceDto(
    id = this.id
)

fun Application.toDto() = ApplicationDto(
    id = this.id,
    name = this.name,
    images = this.images.map { it.toDto() }.toSet()
)

fun Image.toDto() = ImageDto(
    id = this.id,
    name = this.name,
    version = this.version
)