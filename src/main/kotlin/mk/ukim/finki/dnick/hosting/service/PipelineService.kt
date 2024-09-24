package mk.ukim.finki.dnick.hosting.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kubernetes.client.openapi.apis.AppsV1Api
import mk.ukim.finki.dnick.hosting.builder.cleanupK8sName
import mk.ukim.finki.dnick.hosting.controller.ApplicationController
import mk.ukim.finki.dnick.hosting.image.*
import mk.ukim.finki.dnick.hosting.model.domain.Deployment
import mk.ukim.finki.dnick.hosting.model.entity.BaseImageType
import mk.ukim.finki.dnick.hosting.model.entity.EnvironmentValue
import mk.ukim.finki.dnick.hosting.model.entity.Image
import mk.ukim.finki.dnick.hosting.repository.*
import mk.ukim.finki.dnick.hosting.service.ApplicationDeploymentService.PodData
import mk.ukim.finki.dnick.hosting.socket.SocketSessionCache
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.*


private val log = KotlinLogging.logger {}

fun ApplicationDto.cleanUp(): ApplicationDto {
    return ApplicationDto(
        request = ApplicationRequest(
            name = this.request.name.cleanupK8sName(),
            socket = this.request.socket,
            namespace = this.request.namespace.cleanupK8sName(),
            deployments = this.request.deployments.map {
                DeploymentRequest(
                    port = it.port,
                    name = it.name.cleanupK8sName(),
                    image = it.image,
                    service = ServiceDto(
                        port = it.service.port
                    ),
                    ingress = IngressDto(
                        path = it.ingress.path
                    ),
                    environment = it.environment
                )
            }
        ),
        images = images.map {
            it.key to when (it.value) {
                is ImageGitParams -> (it.value as ImageGitParams).copy(
                    data = ImageData(
                        it.value.data.name.cleanupK8sName(),
                        it.value.data.version.cleanupK8sName()
                    )
                )

                is ImageExeParams -> (it.value as ImageExeParams).copy(
                    data = ImageData(
                        it.value.data.name.cleanupK8sName(),
                        it.value.data.version.cleanupK8sName()
                    )
                )

                else -> throw RuntimeException()
            }
        }.toMap()
    )
}

@Service
class PipelineService(
    private val imageBuilderService: ImageBuilderService,
    private val applicationDeploymentService: ApplicationDeploymentService,
    private val applicationPersistenceService: ApplicationPersistenceService,
    private val socketSessionCache: SocketSessionCache,
    private val baseImageRefRepository: BaseImageRefRepository,
    private val imageRepository: ImageRepository,
    private val namespaceRepository: NamespaceRepository,
    private val environmentRepository: EnvironmentRepository,
    private val environmentValueRepository: EnvironmentValueRepository,
) {


    data class DeploymentUpdateDto(
        val version: String,
        val file: MultipartFile?,
    )

    @Transactional
    fun editDeploymentConfig(id: Int, dto: ApplicationController.EditDeploymentDto, deployment: Deployment) {
        deployment.pods.forEach { pod ->
            environmentRepository.findByIdOrNull(pod.environment.id)?.let {
                environmentValueRepository.deleteAll(it.values)
                it.values = mutableSetOf()
                it
            }?.let { env ->
                dto.properties.map {
                    EnvironmentValue(
                        name = it.key,
                        value = it.value,
                        environment = env
                    )
                }
            }?.also {
                environmentValueRepository.saveAll(it)
            }

            applicationDeploymentService.editDeploymentConfig(
                dto,
                pod.name,
                pod.environment.name,
                deployment.name,
                deployment.namespace
            )
        }
    }


    @Transactional
    fun updateAndDeploy(deployment: Deployment, update: DeploymentUpdateDto) {
        deployment.pods.forEach { pod ->
            val imageData = ImageData(pod.image.name, update.version)
            baseImageRefRepository.findByIdOrNull(pod.image.baseRef)?.let {
                when (it.type) {
                    BaseImageType.GIT -> it.baseImageGit?.let { i ->
                        buildImage(
                            ImageGitParams(
                                namespace = deployment.namespace,
                                buildTool = i.buildTool,
                                version = i.version,
                                base = ImageBaseParams(i.base.language, i.base.version),
                                uid = UUID.randomUUID().toString(),
                                data = imageData,
                                buildArgs = pod.image.arguments
                            )
                        )
                    }

                    BaseImageType.EXE -> it.baseImageExe?.let { i ->
                        buildImage(
                            ImageExeParams(
                                namespace = deployment.namespace,
                                base = ImageBaseParams(i.base.language, i.base.version),
                                uid = UUID.randomUUID().toString(),
                                data = imageData,
                                buildArgs = pod.image.arguments,
                                file = update.file!!
                            )
                        )
                    }
                }
                it
            }?.also {
                imageRepository.save(
                    Image(
                        name = imageData.name,
                        version = imageData.version,
                        hash = UUID.randomUUID(),
                        namespace = namespaceRepository.findByName(deployment.namespace)
                            ?: throw RuntimeException("Namespace not found"),
                        base = it,
                        arguments = pod.image.arguments
                    )
                )
            }

            applicationDeploymentService.patchImageVersion(pod.name, imageData, deployment.name, deployment.namespace)
        }
    }

    fun buildAndDeploy(uncleanedApplicationDto: ApplicationDto) {
        val applicationDto = uncleanedApplicationDto.cleanUp();
        val socket = applicationDto.request.socket?.let { socketSessionCache.get(it) }

        log.info { "Creating namespace" }

        applicationDeploymentService.createNamespace(applicationDto.request.namespace)

        log.info { "Created namespace" }

        log.info { "Building images" }
        sendText("Started building", socket)

        applicationDto.images.map {
            sendText("Building ${it.value.data.name}:${it.value.data.version}", socket)
            it.key to buildImage(it.value)
        }.toMap()


        log.info { "Done building images" }
        sendText("Done building", socket)


        log.info { "Persisting applications" }
        sendText("Persisting applications", socket)

        val application = applicationPersistenceService.persistApplication(applicationDto)
        log.info { "Done persisting applications" }
        sendText("Done persisting applications", socket)

        log.info { "Deploying applications to kubernetes" }
        application.deployments.forEach { deployment ->

            sendText("Deploying service: ${deployment.name}", socket)
            log.info { "Deploying ${deployment.name} environment" }

            deployment.pods.forEach { pod ->
                sendText("Providing arguments: ${deployment.name}", socket)
                applicationDeploymentService.createConfigMap(
                    namespace = application.namespace,
                    pod.environment
                )
            }

            log.info { "Deploying application: ${deployment.name} " }
            applicationDeploymentService.createDeployment(application.namespace, deployment)

            log.info { "Deployed application: ${deployment.name}" }
            sendText("Deployed service: ${deployment.name}", socket)
        }

        log.info { "Deployed applications to kubernetes" }


        log.info { "Deploying services to kubernetes" }

        application.services.forEach { service ->
            sendText("Exposing service internally: ${service.name} ", socket)
            log.info { "Deploying services to kubernetes: ${service.name}" }
            applicationDeploymentService.createService(application.namespace, service)
        }

        log.info { "Deployed services to kubernetes" }

        log.info { "Deploying ingresses to kubernetes" }
        application.ingresses.forEach { ingress ->
            sendText("Exposing service: ${ingress.rules.first().path}", socket)
            log.info { "Deploying ingress to kubernetes: ${ingress.name}" }
            applicationDeploymentService.createIngress(application.namespace, ingress)
        }

        log.info { "Deployed ingresses to kubernetes" }
        sendText("Done: ${application.name}", socket)
    }

    private fun buildImage(image: ImageParamsTyped) {
        when (image) {
            is ImageGitParams -> imageBuilderService.createImage(image)
            is ImageExeParams -> imageBuilderService.createImage(image)
        }
    }

    private fun sendText(text: String, socket: WebSocketSession?) {
        // language=json
        socket?.sendMessage(
            TextMessage(
                """
                    {
                        "type": "STRING",
                        "data": "$text"
                    }
                    """.trimIndent()
            )
        )
    }

}

data class IngressData(
    val path: String
)

data class ServiceData(
    val name: String,
    val port: Int,
    val targetPort: Int
)

data class FullApplicationDeployment(
    val deployment: ApplicationDeployment,
    val serviceData: ServiceData,
    val ingress: IngressData,
)

data class ApplicationDeployment(
    val image: ImageParamsTyped,
    val namespace: String,
    val pod: PodData
)

data class ApplicationDeploymentDto(
    val namespace: String,
    val pod: PodData,
    val imageData: ImageData
)

fun ApplicationDeploymentDto.toDomain(image: ImageParamsTyped) = ApplicationDeployment(
    image = image,
    namespace = this.namespace,
    pod = this.pod
)