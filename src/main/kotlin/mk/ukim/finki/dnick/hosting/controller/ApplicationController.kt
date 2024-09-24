package mk.ukim.finki.dnick.hosting.controller

import mk.ukim.finki.dnick.hosting.model.dto.NamespaceDto
import mk.ukim.finki.dnick.hosting.model.dto.toDto
import mk.ukim.finki.dnick.hosting.model.entity.BaseImageType
import mk.ukim.finki.dnick.hosting.model.entity.toDomain
import mk.ukim.finki.dnick.hosting.service.ApplicationDeploymentService
import mk.ukim.finki.dnick.hosting.service.ApplicationPersistenceService
import mk.ukim.finki.dnick.hosting.service.BaseImageService
import mk.ukim.finki.dnick.hosting.service.PipelineService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("$API_V1_PATH/application")
class ApplicationController(
    private val applicationPersistenceService: ApplicationPersistenceService,
    private val baseImageService: BaseImageService,
    private val applicationDeploymentService: ApplicationDeploymentService,
    private val pipelineService: PipelineService
) {

    @GetMapping
    fun getApplications(@RequestParam namespace: String): NamespaceDto {
        return applicationPersistenceService.getApplications(namespace)
            .let { n ->
                n.toDto(
                    baseImageService.findBaseImages(
                        n.applications.flatMap { a -> a.images.map { i -> i.baseRef } }.toSet()
                    ).mapNotNull {
                        when (it.type) {
                            BaseImageType.GIT -> it.baseImageGit?.toDomain()?.toDto()
                            BaseImageType.EXE -> it.baseImageExe?.toDomain()?.toDto()
                        }
                    }.toSet()
                )
            }
    }

    @GetMapping("/deployment/{id}/start")
    fun startDeployment(@PathVariable id: Int) {
        val deployment = applicationPersistenceService.getDeployment(id)
        applicationDeploymentService.startDeployment(deployment.name, deployment.namespace)
    }

    @GetMapping("/deployment/{id}/stop")
    fun stopDeployment(@PathVariable id: Int) {
        val deployment = applicationPersistenceService.getDeployment(id)
        applicationDeploymentService.stopDeployment(deployment.name, deployment.namespace)
    }

    @GetMapping("/deployment/{id}/update")
    fun updateDeployment(@PathVariable id: Int, @RequestParam version: String) {
        val deployment = applicationPersistenceService.getDeployment(id)
        pipelineService.updateAndDeploy(deployment, PipelineService.DeploymentUpdateDto(version, null))
    }

    @PostMapping("/deployment/{id}/edit")
    fun editDeployment(@PathVariable id: Int, @RequestBody dto: EditDeploymentDto) {
        val deployment = applicationPersistenceService.getDeployment(id)
        pipelineService.editDeploymentConfig(id, dto, deployment)
    }

    @GetMapping("/deployment/{id}/update/exe")
    fun updateDeployment(
        @PathVariable id: Int,
        @RequestParam version: String,
        @RequestPart file: MultipartFile
    ) {
        val deployment = applicationPersistenceService.getDeployment(id)
        pipelineService.updateAndDeploy(deployment, PipelineService.DeploymentUpdateDto(version, file))
    }

    @PostMapping
    fun createApplication(request: ApplicationCreateRequest) {

    }

    data class EditDeploymentDto(
        val properties: Map<String, String>
    )

}