package mk.ukim.finki.dnick.hosting.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import mk.ukim.finki.dnick.hosting.generated.model.*
import mk.ukim.finki.dnick.hosting.image.ImageBaseParams
import mk.ukim.finki.dnick.hosting.image.ImageData
import mk.ukim.finki.dnick.hosting.image.ImageExeParams
import mk.ukim.finki.dnick.hosting.image.ImageGitParams
import mk.ukim.finki.dnick.hosting.mapper.toDetailsDto
import mk.ukim.finki.dnick.hosting.mapper.toDto
import mk.ukim.finki.dnick.hosting.model.dto.ImageBuildRequestDto
import mk.ukim.finki.dnick.hosting.service.ImageService
import mk.ukim.finki.dnick.hosting.service.NamespaceService
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import mk.ukim.finki.dnick.hosting.generated.model.Image as ImageDto
import mk.ukim.finki.dnick.hosting.generated.model.ImageLog as ImageLogDto

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("/api/v2/images")
class ImageController(
    private val imageService: ImageService,
    private val namespaceService: NamespaceService
) {

    @GetMapping
//    @PreAuthorize("hasAuthority('image-read')")
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
//    @PreAuthorize("hasAuthority('image-write')")
    fun deleteImage(@PathVariable id: Int): ResponseEntity<Unit> {
        imageService.deleteImage(id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/tag/{id}")
//    @PreAuthorize("hasAuthority('image-write')")
    fun deleteImageTag(@PathVariable id: Int): ResponseEntity<Unit> {
        imageService.deleteImageTag(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasAuthority('image-read')")
    fun getImage(@PathVariable("id") id: Int): ResponseEntity<ImageDetails> {
        return imageService.getImage(id).toDetailsDto().let { ResponseEntity.ok(it) }
    }

    @PostMapping
//    @PreAuthorize("hasAuthority('image-write')")
    fun createImage(@RequestBody imageCreateRequest: ImageCreateRequest): ResponseEntity<ImageDto> {
        val image = imageService.createImage(imageCreateRequest)
        return ResponseEntity.created(UriComponentsBuilder.fromPath("/api/v1/image/${image.id}").build().toUri())
            .body(image.toDto())
    }

    @GetMapping("/{id}/log")
//    @PreAuthorize("hasAuthority('image-read')")
    fun getImageLog(@PathVariable id: Int): ImageLogDto {
        return imageService.getImageLog(id)?.let {
            ImageLogDto(data = it.data)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PatchMapping("/{id}")
    fun editImage(
        @PathVariable id: Int,
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
        namespace = namespaceService.getUserNamespace(),
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
        namespace = namespaceService.getUserNamespace(),
        buildArgs = this.buildArgs
    )
}