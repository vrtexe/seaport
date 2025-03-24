package mk.ukim.finki.dnick.hosting.service

import mk.ukim.finki.dnick.hosting.controller.NamespaceController.CreateNamespaceRequest
import mk.ukim.finki.dnick.hosting.image.ImageParamsTyped
import mk.ukim.finki.dnick.hosting.model.entity.*
import mk.ukim.finki.dnick.hosting.repository.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.*
import mk.ukim.finki.dnick.hosting.model.domain.Application as DomainApplication
import mk.ukim.finki.dnick.hosting.model.domain.Deployment as DomainDeployment
import mk.ukim.finki.dnick.hosting.model.domain.Namespace as DomainNamespace
import org.springframework.stereotype.Service as SpringService

@SpringService
class ApplicationPersistenceService(
    private val applicationRepository: ApplicationRepository,
    private val namespaceRepository: NamespaceRepository,
    private val podRepository: PodRepository,
    private val imageTagRepository: ImageTagRepository,
    private val deploymentRepository: DeploymentRepository,
    private val environmentRepository: EnvironmentRepository,
    private val environmentValueRepository: EnvironmentValueRepository,
    private val serviceRepository: ServiceRepository,
    private val servicePortRepository: ServicePortRepository,
    private val ingressRepository: IngressRepository,
    private val ingressRuleRepository: IngressRuleRepository,
    private val imageService: ImageService,
    private val imageRepository: ImageRepository,
) {

    private fun namespaceFromRequest(request: CreateNamespaceRequest) = Namespace(name = request.name)

    @Transactional
    fun getApplications(namespace: String): DomainNamespace {
        return getNamespaceByName(namespace)
    }

    @Transactional(readOnly = true)
    fun getDeployment(id: Int): DomainDeployment {
        return deploymentRepository.findByIdOrNull(id)?.toDomain()
            ?: throw ResponseStatusException(BAD_REQUEST, "Missing deployment")
    }

    @Transactional
    fun createNamespace(request: CreateNamespaceRequest): DomainNamespace {
        namespaceRepository.findByName(request.name)?.let {
            return it.toDomain()
        }

        return namespaceRepository.save(namespaceFromRequest(request)).toDomain()
    }

    @Transactional
    fun createNamespace(name: String): Namespace {
        namespaceRepository.findByName(name)?.let {
            return it
        }

        return namespaceRepository.save(Namespace(name = name))
    }

    @Transactional(readOnly = true)
    fun getNamespace(uid: String): DomainNamespace {
        return namespaceRepository.findByIdOrNull(UUID.fromString(uid))?.toDomain()
            ?: throw ResponseStatusException(NOT_FOUND, "Resource not found")
    }

    @Transactional(readOnly = true)
    fun getNamespaceByName(name: String): DomainNamespace {
        return namespaceRepository.findByName(name)?.toDomain()
            ?: throw ResponseStatusException(NOT_FOUND, "Resource not found")
    }


    private fun createImageCache(images: Map<String, ImageParamsTyped>): (uid: String) -> ImageTag {
        val state = mutableMapOf<String, ImageTag>()

        return { uid ->
            if (state[uid] != null) {
                state[uid]
            }

            val image = images[uid] ?: throw ResponseStatusException(BAD_REQUEST, "Image not found")
            state[uid] = imageService.saveImage(image, UUID.fromString(image.uid))
            state[uid] ?: throw ResponseStatusException(BAD_REQUEST, "Image not found")
        }

    }

    @Transactional
    fun persistApplication(dto: ApplicationDto): DomainApplication {
        val data = dto.request

        if (applicationRepository.findByNameContainingIgnoreCase(data.name) != null) {
            throw ResponseStatusException(BAD_REQUEST, "Application already exists")
        }

        val namespace = createNamespace(data.namespace)
        val application = applicationRepository.save(Application(name = data.name, namespace = namespace))

        val imageHandler = createImageCache(dto.images)

        dto.request.deployments.forEach {
            val image = imageHandler(it.image)

            val deployment = deploymentRepository.save(
                Deployment(
                    name = it.name,
                    application = application
                )
            )

            val env = environmentRepository.save(
                Environment(
                    name = "${it.name}-environment"
                )
            )

            environmentValueRepository.saveAll(
                it.environment.values.map { e ->
                    EnvironmentValue(
                        name = e.key,
                        value = e.value,
                        environment = env
                    )
                }
            )

            val pod = podRepository.save(
                Pod(
                    name = "${it.name}-pod",
                    workdir = "",
                    activeImage = image,
                    deployment = deployment,
                    environment = env,
                    port = it.port
                )
            )

            val service = serviceRepository.save(
                Service(
                    name = "${it.name}-service",
                    deployment = deployment,
                    application = application,
                )
            )

            val servicePort = servicePortRepository.save(
                ServicePort(
                    name = "${it.name}-service-port",
                    port = 80,
                    service = service,
                    pod = pod
                )
            )

            val ingress = ingressRepository.save(
                Ingress(
                    name = "${it.name}-ingress",
                    application = application
                )
            )

            ingressRuleRepository.save(
                IngressRule(
                    path = it.ingress.path,
                    servicePort = servicePort,
                    ingress = ingress
                )
            )
        }

        return applicationRepository.findById(application.id!!).orElse(null)?.toDomain()!!
    }

    @Transactional
    fun create(): DomainApplication? {
        val namespace = namespaceRepository.save(Namespace(name = "test-namespace-${UUID.randomUUID()}"))
        val application = applicationRepository.save(
            Application(
                name = "test-application-${UUID.randomUUID()}",
                namespace = namespace,
            )
        )

        val image = imageRepository.save(
            Image(
                name = "test-image",
                namespace = namespace,
            )
        )

        val imageTag = imageTagRepository.save(
            ImageTag(
                version = "1.0",
                arguments = mapOf(
                    "name" to "test-image.com",
                ),
                image = image,
                status = ImageStatus.initialized,
                base = BaseImageRef(baseImageExe = null, baseImageGit = null),
            )
        )

        val deployment = deploymentRepository.save(
            Deployment(
                name = "test-deployment",
                application = application,
            )
        )

        val environment = environmentRepository.save(Environment(name = "test-environment"))

        val environmentValues = environmentValueRepository.saveAll(
            mutableListOf(
                EnvironmentValue(
                    name = "JAR_ARGS",
                    value = "-Dspring.profile=prod",
                    environment = environment
                ),
                EnvironmentValue(
                    name = "DB_PASSWORD",
                    value = "password",
                    environment = environment
                )
            )
        )

        val Pod = podRepository.save(
            Pod(
                name = "test-pod",
                workdir = ".",
                activeImage = imageTag,
                deployment = deployment,
                environment = environment,
                port = 8080
            )
        )

        val service = serviceRepository.save(
            Service(
                name = "test-service",
                deployment = deployment,
                application = application,
            )
        )

        val servicePort = servicePortRepository.save(
            ServicePort(
                name = "test-service-port",
                port = 80,
                service = service,
                pod = Pod
            )
        )

        val ingress = ingressRepository.save(
            Ingress(
                name = "test-ingress",
                application = application
            )
        )

        val ingressRule = ingressRuleRepository.save(
            IngressRule(
                path = "/test",
                servicePort = servicePort,
                ingress = ingress
            )
        )

        return applicationRepository.findById(application.id!!).orElse(null)?.toDomain()
    }
}

data class ApplicationRequest(
    val name: String,
    val socket: String?,
    val namespace: String,
    val deployments: List<DeploymentRequest>
)

data class DeploymentRequest(
    val port: Int,
    val name: String,
    val image: String,
    val service: ServiceDto,
    val ingress: IngressDto,
    val environment: EnvironmentDto
)

fun ApplicationRequest.toDto(images: List<ImageParamsTyped>) = ApplicationDto(
    request = this,
    images = images.associateBy { it.uid }
)

data class ApplicationDto(
    val request: ApplicationRequest,
    val images: Map<String, ImageParamsTyped>
)

data class EnvironmentDto(
    val values: Map<String, String>
)

data class IngressDto(
    val path: String,
)

data class ServiceDto(
    val port: Int
)

data class ImageDto(
    val name: String,
    val version: String,
    val hash: String,
    val baseImage: Int,
    val buildArgs: Map<String, String>
)