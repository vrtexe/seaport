package mk.ukim.finki.dnick.hosting.model.dto

import mk.ukim.finki.dnick.hosting.model.domain.*

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
    val images: Set<ImageDto>,
    val deployments: List<DeploymentDto>,
    val baseImages: Set<BaseImageDto>
)

data class BaseImageGitDto(
    val buildTool: String,
    val buildToolVersion: String,
    override val id: Int,
    override val language: String,
    override val languageVersion: String?,
) : BaseImageDto(ImageTypeDto.GIT)

data class BaseImageExeDto(
    override val id: Int,
    override val language: String,
    override val languageVersion: String?
) : BaseImageDto(ImageTypeDto.EXE)

abstract class BaseImageDto(
    val type: ImageTypeDto
) {
    abstract val id: Int;
    abstract val language: String;
    abstract val languageVersion: String?;
}

enum class ImageTypeDto {
    GIT, EXE
}

data class DeploymentDto(
    val id: Int,
    val name: String,
    val pods: Set<PodDto>,
    val ingresses: Set<IngressDto>
)

data class ServiceDto(
    val id: Int,
    val name: String,
    val port: Set<ServicePortDto>
)

data class ServicePortDto(
    val name: String,
    val port: Int,
    val targetPort: Int
)

data class IngressDto(
    val id: Int,
    val ingressRules: List<IngressRuleDto>
)

data class IngressRuleDto(
    val path: String,
    val service: ServiceDto,
)

data class PodDto(
    val id: Int,
    val name: String,
    val port: Int,
    val image: ImageDto,
    val environment: EnvironmentDto
)

data class EnvironmentDto(
    val id: Int,
    val name: String,
    val content: Map<String, String>
)

data class ImageDto(
    val id: Int,
    val name: String,
    var version: String,
    val hash: String,
    val baseRef: Int,
    val arguments: Map<String, String>
)

fun Namespace.toDto(baseImages: Set<BaseImageDto> = setOf()) = NamespaceDto(
    uid = this.id.toString(),
    name = this.name,
    applications = this.applications.map { it.toDto(baseImages) }.toSet(),
    externalService = this.externalServices.map { it.toDto() }.toSet(),
)

fun ExternalService.toDto() = ExternalServiceDto(
    id = this.id
)

fun Application.toDto(baseImages: Set<BaseImageDto> = setOf()) = ApplicationDto(
    id = this.id,
    name = this.name,
    images = this.images.map { it.toDto() }.toSet(),
    deployments = this.deployments.map { d ->
        DeploymentDto(
            id = d.id,
            name = d.name,
            pods = d.pods.map {
                PodDto(
                    id = it.id,
                    name = it.name,
                    port = it.port,
                    image = it.image.toDto(),
                    environment = it.environment.toDto()
                )
            }.toSet(),
            ingresses = this.services
                .filter { s -> s.deployment == d.name }
                .flatMap {
                    this.ingresses.filter { i ->
                        i.rules.any { r ->
                            r.servicePort.serviceName == it.name
                        }
                    }.map { i ->
                        IngressDto(
                            id = i.id,
                            ingressRules = i.rules.map { r ->
                                IngressRuleDto(
                                    path = r.path,
                                    service = it.toDto()
                                )
                            }
                        )
                    }
                }.toSet()
        )
    },
    baseImages = baseImages
)

fun Image.toDto() = ImageDto(
    id = this.id,
    name = this.name,
    version = this.version,
    hash = this.hash.toString(),
    baseRef = this.baseRef,
    arguments = this.arguments,
)

fun Service.toDto() = ServiceDto(
    id = this.id,
    name = this.name,
    port = this.ports.map { it.toDto() }.toSet()
)

fun ServicePort.toDto() = ServicePortDto(
    name = this.name,
    port = this.port,
    targetPort = this.targetPort
)

fun BaseImageGit.toDto() = BaseImageGitDto(
    buildTool = this.buildTool,
    buildToolVersion = this.version,
    id = this.refId,
    language = this.base.language,
    languageVersion = this.base.version
)


fun BaseImageExe.toDto() = BaseImageExeDto(
    id = this.refId,
    language = this.base.language,
    languageVersion = this.base.version
)

fun Environment.toDto() = EnvironmentDto(
    id = this.id,
    name = this.name,
    content = this.content
)