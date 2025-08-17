package mk.ukim.finki.dnick.hosting.controller

import jakarta.validation.Valid
import mk.ukim.finki.dnick.hosting.generated.model.*
import mk.ukim.finki.dnick.hosting.infra.config.KubernetesProperties
import mk.ukim.finki.dnick.hosting.service.DeploymentService
import mk.ukim.finki.dnick.hosting.service.NamespaceService
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.ZoneOffset
import mk.ukim.finki.dnick.hosting.generated.model.DeploymentService as ModelDeploymentService
import mk.ukim.finki.dnick.hosting.model.domain.DeploymentState.Companion as DeploymentStateDomain
import mk.ukim.finki.dnick.hosting.model.entity.Deployment as DeploymentEntity

@RestController
@RequestMapping("/api/v2/deployments")
class DeploymentController(
    private val deploymentService: DeploymentService,
    private val kubernetesProperties: KubernetesProperties,
    private val namespaceService: NamespaceService,
) {

    @PostMapping
    fun createDeployment(@RequestBody @Valid request: DeploymentCreateRequest): ResponseEntity<Unit> {
        deploymentService.createDeployment(request)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{id}")
    fun getDeployment(@PathVariable id: Int): ResponseEntity<DeploymentDetails> {
        return ResponseEntity.ok(deploymentService.getDeployment(id).toDetailResponse())
    }

    @DeleteMapping("/{id}")
    fun deleteDeployment(@PathVariable id: Int): ResponseEntity<Unit> {
        deploymentService.deleteDeployment(id)
        return ResponseEntity.ok().build()
    }

    @GetMapping
    fun getDeployments(
        @Valid criteria: DeploymentCriteria,
        @Valid pageable: Pageable = Pageable.unpaged()
    ): ResponseEntity<DeploymentsResponse> {
        return ResponseEntity.ok(deploymentService.getDeployments(criteria, Pageable.unpaged()).let {
            DeploymentsResponse(
                it.map { deployment -> deployment.toResponse() }.toList(),
                metadata = ResponseMetadata(
                    pagination = it.toPagination()
                )
            )
        })
    }

    @PatchMapping("/{id}/state")
    fun updateDeploymentState(
        @PathVariable id: Int,
        @RequestBody @Valid state: DeploymentState
    ): ResponseEntity<DeploymentDetails> {
        deploymentService.setDeploymentState(
            id,
            state.toDomain() ?: throw ResponseStatusException(BAD_REQUEST, "Invalid state")
        );
        return ResponseEntity.noContent().build()
    }

    private fun DeploymentEntity.toDetailResponse() = DeploymentDetails(
        id = this.id!!,
        uid = this.uid.toString(),
        general = DeploymentDetailsGeneral(
            name = this.name,
            port = this.pods.first().port,
        ),
        service = this.pods.first().servicePorts.first().service.let {
            DeploymentDetailsService(
                name = it.name
            )
        },
        group = Group(
            id = this.application.id!!,
            name = this.application.name,
        ),
        imageTag = this.pods.first().activeImage.let {
            ImageTag(
                id = it.id!!,
                uid = it.hash.toString(),
                version = it.version,
                created = it.createdAt.atOffset(ZoneOffset.UTC),
                status = ImageTagStatus.forValue(it.status.toString().uppercase())
            )
        },
        environment = this.pods.first().environment.values.map { it.name to it.value }.toMap(),
        ingress = this.pods.first().servicePorts.first().ingressRules.firstOrNull()?.let { ir ->
            DeploymentCreateRequestIngress(
                name = ir.ingress.name,
                path = ir.path
            )
        },
    )

    private fun DeploymentEntity.toResponse(): Deployment {
        return Deployment(
            id = this.id!!,
            uid = this.uid.toString(),
            name = this.name,
            cluster = DeploymentCluster(
                url = kubernetesProperties.url,
                namespace = namespaceService.getUserNamespace()
            ),
            image = this.pods.first().activeImage.let { i ->
                DeploymentImage(
                    id = i.image.id!!,
                    name = i.image.name,
                    tag = DeploymentImageTag(
                        id = i.id!!,
                        version = i.version
                    ),
                )
            },
            service = this.pods.first().servicePorts.first().let {
                ModelDeploymentService(
                    port = it.port,
                    id = it.service.id,
                    name = it.service.name
                )
            },
            state = DeploymentState.forValue(this.state.name.uppercase()),
            group = DeploymentGroup(
                id = this.application.id!!,
                name = this.application.name,
            ),
            ingress = this.pods.first().servicePorts.first().ingressRules.firstOrNull()?.let { ir ->
                DeploymentIngress(
                    id = ir.ingress.id!!,
                    path = ir.path
                )
            },
        )
    }

    @PutMapping("/{id}")
    fun updateDeployment(
        @PathVariable id: Int,
        @RequestBody @Valid request: DeploymentCreateRequest
    ): ResponseEntity<Unit> {
        deploymentService.updateDeployment(id, request)
        return ResponseEntity.ok().build()
    }

    private fun DeploymentState.toDomain() = DeploymentStateDomain.of(this.value)
}

