package mk.ukim.finki.dnick.hosting.controller

import jakarta.validation.Valid
import mk.ukim.finki.dnick.hosting.generated.model.*
import mk.ukim.finki.dnick.hosting.infra.config.KubernetesProperties
import mk.ukim.finki.dnick.hosting.service.DeploymentService
import mk.ukim.finki.dnick.hosting.service.NamespaceService
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import mk.ukim.finki.dnick.hosting.generated.model.DeploymentService as ModelDeploymentService

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


    @DeleteMapping("/{id}")
    fun deleteDeployment(@PathVariable id: Int): ResponseEntity<Unit> {
        deploymentService.deleteDeployment(id)
        return ResponseEntity.ok().build()
    }

    @GetMapping
    fun getDeployments(): ResponseEntity<DeploymentsResponse> {
        return ResponseEntity.ok(deploymentService.getDeployments(Pageable.unpaged()).let {
            DeploymentsResponse(
                it.map { deployment ->
                    Deployment(
                        id = deployment.id!!,
                        uid = deployment.uid.toString(),
                        name = deployment.name,
                        cluster = DeploymentCluster(
                            url = kubernetesProperties.url,
                            namespace = namespaceService.getUserNamespace()
                        ),
                        image = deployment.pods.first().activeImage.let { i ->
                            DeploymentImage(
                                id = i.image.id!!,
                                name = i.image.name,
                                tag = DeploymentImageTag(
                                    id = i.id!!,
                                    version = i.version
                                ),
                            )
                        },
                        service = deployment.pods.first().servicePorts.first().let {
                            ModelDeploymentService(
                                port = it.port,
                                id = it.service.id,
                                name = it.service.name
                            )
                        },
                        state = DeploymentState.forValue(deployment.state.name.uppercase()),
                        group = DeploymentGroup(
                            id = deployment.application.id!!,
                            name = deployment.application.name,
                        ),
                        ingress = deployment.pods.first().servicePorts.first().ingressRules.firstOrNull()?.let { ir ->
                            DeploymentIngress(
                                id = ir.ingress.id!!,
                                path = ir.path
                            )
                        },
                    )
                }.toList(),
                metadata = ResponseMetadata(
                    pagination = it.toPagination()
                )
            )
        })
    }

    @PatchMapping("/{id}")
    fun updateDeployment(
        @PathVariable id: Int,
        @RequestBody @Valid request: DeploymentCreateRequest
    ): ResponseEntity<Unit> {
        deploymentService.updateDeployment(id, request)
        return ResponseEntity.ok().build()
    }

}