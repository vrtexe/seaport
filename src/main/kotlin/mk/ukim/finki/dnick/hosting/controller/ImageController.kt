package mk.ukim.finki.dnick.hosting.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import mk.ukim.finki.dnick.hosting.generated.model.*
import mk.ukim.finki.dnick.hosting.image.*
import mk.ukim.finki.dnick.hosting.infra.config.UserProperties
import mk.ukim.finki.dnick.hosting.model.dto.ImageBuildRequestDto
import mk.ukim.finki.dnick.hosting.model.entity.*
import mk.ukim.finki.dnick.hosting.model.entity.BaseImageType
import mk.ukim.finki.dnick.hosting.model.entity.ImageTag
import mk.ukim.finki.dnick.hosting.service.ImageService
import mk.ukim.finki.dnick.hosting.service.InternalArgument
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import java.time.ZoneId
import java.util.*
import mk.ukim.finki.dnick.hosting.generated.model.BaseImageType.Companion as BaseImageTypeDto
import mk.ukim.finki.dnick.hosting.generated.model.Image as ImageDto
import mk.ukim.finki.dnick.hosting.generated.model.ImageLog as ImageLogDto
import mk.ukim.finki.dnick.hosting.generated.model.ImageTag as ImageTagDto

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("/api/v2/images")
class ImageController(
    private val imageService: ImageService,
    private val userProperties: UserProperties
) {

    @GetMapping
    fun getImages(pageable: Pageable = Pageable.unpaged()): ResponseEntity<ImagesResponse> {
        return ResponseEntity.ok(
            imageService.getImages(pageable).let {
                ImagesResponse(
                    data = it.content.map { it.toDto() },
                    metadata = ResponseMetadata(
                        pagination = it.toPagination()
                    )
                )
            }
        )
    }

    @DeleteMapping("/{id}")
    fun deleteImage(@PathVariable("id") id: Int): ResponseEntity<Unit> {
        imageService.deleteImage(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}")
    fun getImage(@PathVariable("id") id: Int): ResponseEntity<ImageDetails> {
        return imageService.getImage(id).toDetailsDto().let { ResponseEntity.ok(it) }
    }

    @PostMapping
    fun createImage(@RequestBody imageCreateRequest: ImageCreateRequest): ResponseEntity<ImageDto> {
        val image = imageService.createImage(imageCreateRequest)
        return ResponseEntity.created(UriComponentsBuilder.fromPath("/api/v1/image/${image.id}").build().toUri())
            .body(image.toDto())
    }

    @GetMapping("/{id}/log")
    fun getImageLog(@PathVariable("id") id: Int): ImageLogDto {
        return imageService.getImageLog(id)?.let {
            ImageLogDto(data = it.data)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PatchMapping("/{id}")
    fun editImage(
        @PathVariable("id") id: Int,
        @RequestBody imageUpdateRequest: ImageUpdateRequest
    ): ResponseEntity<ImageDto> {
        val image = imageService.editImage(id, imageUpdateRequest)
        return ResponseEntity.ok(image.toDto())
    }

    @PostMapping("{id}/exe/create")
    fun buildImage(
        @PathVariable id: Int,
        @RequestParam @Valid body: ImageExeRequest,
        @RequestPart files: List<MultipartFile>
    ) {
        imageService.buildImage(
            ImageBuildRequestDto(
                imageId = id,
                socket = body.socket,
                request = body.toParams(files.first())
            )
        )
    }

    @PostMapping("{id}/git/create")
    fun buildImage(@PathVariable id: Int, @RequestBody @Valid body: ImageGitRequest) {
        imageService.buildImage(
            ImageBuildRequestDto(
                imageId = id,
                request = body.toParams(),
                socket = body.socket
            )
        )
    }

    fun ImageExeRequest.toParams(file: MultipartFile) = ImageExeParams(
        uid = UUID.randomUUID().toString(),
        namespace = userProperties.namespace,
        file = file,
        base = this.base,
        data = this.data,
        buildArgs = this.buildArgs
    )

    data class ImageExeRequest(
        @field:Valid val data: ImageData,
        val base: ImageBaseParams,
        val socket: String?,
        val buildArgs: Map<String, String> = mapOf()
    )

    data class ImageGitRequest(
        @field:NotBlank val buildTool: String,
        @field:NotBlank val version: String,
        @field:Valid val base: ImageBaseParams,
        @field:Valid val data: ImageData,
        val buildArgs: Map<String, String> = mapOf(),
        val socket: String?
    )

    fun ImageGitRequest.toParams() = ImageGitParams(
        uid = UUID.randomUUID().toString(),
        buildTool = this.buildTool,
        version = this.version,
        base = this.base,
        data = this.data,
        namespace = userProperties.namespace,
        buildArgs = this.buildArgs
    )

    fun Image.toDto() = ImageDto(
        id = this.id!!,
        name = this.name,
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

    fun BaseImageRef.toDetailsDto() = ImageTagDetailsBase(
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
            ImageTagDetailsBaseGit(
                buildTool = it.buildTool,
                buildToolVersion = it.version
            )
        },
        arguments = this.arguments.map {
            ImageTagDetailsBaseArgument(
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

}