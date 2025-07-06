package mk.ukim.finki.dnick.hosting.service

import mk.ukim.finki.dnick.hosting.deployment.DeploymentQueue
import mk.ukim.finki.dnick.hosting.generated.model.DeploymentCreateRequest
import mk.ukim.finki.dnick.hosting.generated.model.DeploymentCreateRequestDeployment
import mk.ukim.finki.dnick.hosting.generated.model.DeploymentCreateRequestIngress
import mk.ukim.finki.dnick.hosting.generated.model.DeploymentCreateRequestService
import mk.ukim.finki.dnick.hosting.model.domain.PartialDeployment
import mk.ukim.finki.dnick.hosting.model.entity.*
import mk.ukim.finki.dnick.hosting.model.entity.Deployment
import mk.ukim.finki.dnick.hosting.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.*
import mk.ukim.finki.dnick.hosting.deployment.Deployment as QueuedDeployment
import mk.ukim.finki.dnick.hosting.generated.model.DeploymentState as DeploymentStateApi
import mk.ukim.finki.dnick.hosting.model.domain.DeploymentState as DeploymentStateDomain
import mk.ukim.finki.dnick.hosting.model.entity.Service as ServiceEntity

@Service
class DeploymentService(
    private val applicationRepository: ApplicationRepository,
    private val imageTagRepository: ImageTagRepository,
    private val deploymentRepository: DeploymentRepository,
    private val environmentRepository: EnvironmentRepository,
    private val environmentValueRepository: EnvironmentValueRepository,
    private val podRepository: PodRepository,
    private val serviceRepository: ServiceRepository,
    private val servicePortRepository: ServicePortRepository,
    private val ingressRepository: IngressRepository,
    private val ingressRuleRepository: IngressRuleRepository,
    private val namespaceService: NamespaceService,
    private val deliveryService: DeliveryService,
    private val deploymentQueue: DeploymentQueue
) {

    companion object {
        const val ENVIRONMENT_SUFFIX = "-environment"
        const val POD_SUFFIX = "-pod"
    }

    @Transactional
    fun getDeployments(pageable: Pageable): Page<Deployment> {
        return deploymentRepository.findAllByNamespace(namespaceService.getUserNamespace(), pageable)
    }

    @Transactional
    fun createDeployment(request: DeploymentCreateRequest) {
        persistDeployment(request)
        conditionallyTriggerDeployment(request)
    }

    private fun conditionallyTriggerDeployment(request: DeploymentCreateRequest) {
        when (request.deployment.state) {
            DeploymentStateApi.STARTED,
            DeploymentStateApi.INITIAL -> triggerDeployment(request)

            else -> {}
        }
    }

    private fun triggerDeployment(request: DeploymentCreateRequest) {
        deploymentQueue.queueDeployment(
            getDeploymentData(request).let {
                QueuedDeployment(
                    uid = UUID.fromString(it.deployment.uid),
                    data = it,
                    status = DeploymentStateDomain.INITIAL,
                )
            }
        )
    }

    @Transactional
    fun deleteDeployment(id: Int) {
        val deployment = deploymentRepository.findByIdOrNull(id) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Deployment with id: $id not found"
        )

        val namespace = namespaceService.resolveUserNamespace()


        if (deployment.application.namespace.id != namespace.id) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Deployment with id: $id not found")
        }

        deployment.pods.forEach { pod ->
            pod.servicePorts.forEach { servicePort ->
                servicePort.ingressRules.forEach { ingressRule ->
                    ingressRule.id?.let { ingressRuleRepository.deleteById(it) }
                }

                servicePort.ingressRules.first().ingress.id?.let { ingressRepository.deleteById(it) }
            }

            pod.servicePorts.forEach { servicePort -> servicePort.id?.let { servicePortRepository.deleteById(it) } }
            pod.servicePorts.forEach { servicePort -> servicePort.service.id?.let { serviceRepository.deleteById(it) } }
        }

        deployment.pods.forEach { pod ->
            pod.id?.let { podRepository.deleteById(it) }
        }

        deployment.pods.forEach { pod ->
            pod.environment.values.forEach { environmentValue ->
                environmentValue.id?.let { environmentValueRepository.deleteById(it) }
            }

            pod.environment.id?.let { environmentRepository.deleteById(it) }
        }

        deploymentRepository.deleteById(id)
    }

    private fun persistDeployment(request: DeploymentCreateRequest) {
        val application = applicationRepository.findByIdOrNull(request.groupId) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Group with id: ${request.groupId} not found"
        )

        val imageTag = imageTagRepository.findByIdOrNull(request.deployment.imageTagId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Application with id: ${request.deployment.imageTagId} not found"
            )

        deploymentRepository.findByName(request.deployment.name)?.let {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Deployment with name: ${it.name} already exists"
            )
        }
        val deployment = deploymentRepository.save(request.deployment.toEntity(application))

        val environment = createEnvironment(request)
        val pod = podRepository.save(request.deployment.toPod(deployment, imageTag, environment))

        serviceRepository.findByName(request.service.name)?.let {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Service with name: ${it.name} already exists"
            )
        }
        val service = serviceRepository.save(request.service.toEntity(deployment, application))
        val servicePort = servicePortRepository.save(request.service.toServicePort(pod, service))

        request.ingress?.let {
            ingressRepository.findByName(it.name)?.let {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "External service with name: ${it.name} already exists"
                )
            }
            val ingress = ingressRepository.save(it.toEntity(application))
            ingressRuleRepository.save(it.toIngressRule(servicePort, ingress))
        }
    }

    @Transactional
    fun updateDeployment(id: Int, request: DeploymentCreateRequest) {
        val data = persistDeploymentUpdate(id, request)
        deliveryService.updateDeployment(data)
    }

    fun persistDeploymentUpdate(id: Int, request: DeploymentCreateRequest): ResourceChanges {
        val changes = mutableSetOf<Change>()

        val deployment = deploymentRepository.findByIdOrNull(id) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Deployment with id: ${id} not found"
        )

        val application = applicationRepository.findByIdOrNull(request.groupId) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Group with id: ${request.groupId} not found"
        )

        deployment.application = application

        val deploymentName = deployment.name
        if (deployment.name != request.deployment.name) {
            changes.add(Change.DEPLOYMENT_NAME)
            deployment.name = request.deployment.name
        }

        val newState = DeploymentState.valueOf(request.deployment.state.value.lowercase());

        if (newState == DeploymentState.failed) {
            throw IllegalArgumentException("Invalid deployment state")
        }

        if (deployment.state != newState) {
            changes.add(Change.DEPLOYMENT_STATE)
            deployment.state = newState
        }

        var ingressName: String? = null
        var serviceName: String? = null
        var environmentName: String? = null

        deployment.pods.forEach {
            it.name = "$deploymentName$POD_SUFFIX";
            it.servicePorts.forEach { servicePort ->
                serviceName = servicePort.service.name
                if (servicePort.service.name != request.service.name) {
                    changes.add(Change.SERVICE_NAME)
                    servicePort.service.name = request.service.name
                }
                request.ingress?.let { requestIngress ->
                    servicePort.ingressRules.forEach { ingressRule ->
                        if (ingressRule.path != requestIngress.path) {
                            changes.add(Change.INGRESS)
                            ingressRule.path = requestIngress.path
                        }
                        if (ingressRule.ingress.name != requestIngress.name) {
                            ingressName = ingressRule.ingress.name
                            changes.add(Change.INGRESS_NAME)
                            ingressRule.ingress.name = requestIngress.name
                        }
                    }
                } ?: servicePort.ingressRules.forEach { ingressRule ->
                    ingressRule.ingress.id?.let { id -> ingressRepository.deleteById(id) }
                }.also {
                    servicePort.ingressRules.clear()
                    changes.add(Change.INGRESS_DELETE)
                }
            }


            val imageTag = imageTagRepository.findByIdOrNull(request.deployment.imageTagId)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Application with id: ${request.deployment.imageTagId} not found"
                )

            if (it.activeImage.id != imageTag.id) {
                changes.add(Change.DEPLOYMENT_IMAGE)
                it.activeImage = imageTag
            }

            val combinedProps = it.environment.values.map { prop -> prop.name to prop.value }.toSet()
                .union(request.deployment.environment.entries.map { prop -> prop.key to prop.value }.toSet())

            if (combinedProps.size != it.environment.values.size) {
                it.environment.name = "$deploymentName$ENVIRONMENT_SUFFIX";
                it.environment.values.forEach { prop -> prop.id?.let { it1 -> environmentValueRepository.deleteById(it1) } }
                it.environment.values.clear()
                environmentName = it.environment.name

                val environmentValues = request.deployment.environment.entries.map { prop ->
                    EnvironmentValue(
                        name = prop.key,
                        value = prop.value,
                        environment = it.environment
                    )
                }

                it.environment.values.addAll(environmentValues)
                changes.add(Change.ENVIRONMENT_VALUES)
            }

        }

        deploymentRepository.save(deployment)

        return ResourceChanges(
            namespace = namespaceService.getUserNamespace(),
            names = ResourceNames(
                deployment = deploymentName,
                service = serviceName!!,
                ingress = ingressName,
                pod = "$deploymentName$POD_SUFFIX",
                config = "$deploymentName$ENVIRONMENT_SUFFIX"
            ),
            changes = changes.toSet(),
            update = ResourceNames(
                deployment = request.deployment.name,
                service = request.service.name,
                ingress = request.ingress?.name,
                pod = "${request.deployment.name}$POD_SUFFIX",
                config = "${request.deployment.name}$ENVIRONMENT_SUFFIX"
            ),
            state = getDeploymentData(request),
        )
    }

    fun getDeploymentData(request: DeploymentCreateRequest): PartialDeployment {
        return PartialDeployment(
            namespace = namespaceService.getUserNamespace(),
            deployment = deploymentRepository.findByName(request.deployment.name)?.toDomain()!!,
            service = serviceRepository.findByName(request.service.name)?.toDomain()!!,
            ingress = request.ingress?.let { ingressRepository.findByName(it.name) }?.toDomain(),
        )
    }

    @Transactional(readOnly = true)
    fun fetchDeploymentData(uid: UUID): PartialDeployment? {
        return deploymentRepository.findByUid(uid)?.let { deployment ->
            PartialDeployment(
                namespace = namespaceService.getUserNamespace(),
                deployment = deployment.toDomain(),
                service = deployment.getDeploymentService().toDomain(),
                ingress = deployment.getDeploymentIngress()?.toDomain(),
            )
        }
    }

    private fun Deployment.getDeploymentIngress(): Ingress? {
        return this.pods.first().servicePorts.first().ingressRules.firstOrNull()?.ingress
    }

    private fun Deployment.getDeploymentService(): ServiceEntity {
        return this.pods.first().servicePorts.first().service
    }

    fun DeploymentCreateRequestService.toEntity(deployment: Deployment, application: Application) = ServiceEntity(
        name = this.name,
        deployment = deployment,
        application = application
    )

    fun DeploymentCreateRequestIngress.toEntity(application: Application) = Ingress(
        name = this.name,
        application = application
    )

    fun DeploymentCreateRequestIngress.toIngressRule(port: ServicePort, ingress: Ingress) = IngressRule(
        path = this.path,
        servicePort = port,
        ingress = ingress
    )

    fun DeploymentCreateRequestService.toServicePort(pod: Pod, service: ServiceEntity) =
        ServicePort(
            name = "${this.name}-port",
            port = 80,
            service = service,
            pod = pod
        )

    fun createEnvironment(request: DeploymentCreateRequest): Environment {
        val environment =
            environmentRepository.save(Environment(name = "${request.deployment.name}$ENVIRONMENT_SUFFIX"))

        environment.values = environmentValueRepository.saveAll(
            request.deployment.environment.entries.map { e ->
                EnvironmentValue(
                    name = e.key,
                    value = e.value,
                    environment = environment
                )
            }
        ).toMutableSet()

        return environment
    }


    fun DeploymentCreateRequestDeployment.toEntity(application: Application) =
        Deployment(
            name = this.name,
            application = application,
            state = DeploymentState.valueOf(this.state.value.lowercase())
        )


    fun DeploymentCreateRequestDeployment.toPod(
        deployment: Deployment,
        imageTag: ImageTag,
        environment: Environment
    ) =
        Pod(
            name = "${this.name}$POD_SUFFIX",
            workdir = "",
            activeImage = imageTag,
            deployment = deployment,
            environment = environment,
            port = this.exposedPort
        )


}