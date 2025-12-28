package mk.ukim.finki.dnick.hosting.registry

import mk.ukim.finki.dnick.hosting.controller.toPagination
import mk.ukim.finki.dnick.hosting.generated.model.ImageDetails
import mk.ukim.finki.dnick.hosting.generated.model.ImagesResponse
import mk.ukim.finki.dnick.hosting.generated.model.ResponseMetadata
import mk.ukim.finki.dnick.hosting.mapper.toDetailsDto
import mk.ukim.finki.dnick.hosting.mapper.toDto
import mk.ukim.finki.dnick.hosting.model.dto.ImageFilterCriteria
import mk.ukim.finki.dnick.hosting.service.ImageService
import mk.ukim.finki.dnick.hosting.service.NamespaceService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import mk.ukim.finki.dnick.hosting.generated.model.ImageLog as ImageLogDto


@RestController
@RequestMapping("/api/v2/registry")
class RegistryController(private val imageService: ImageService, private val namespaceService: NamespaceService) {

    @GetMapping("/images")
    fun getImages(
        criteria: ImageFilterCriteria?,
        pageable: Pageable
    ): ResponseEntity<ImagesResponse> {
        return ResponseEntity.ok(imageService.getAllImages(criteria, pageable).let {
            ImagesResponse(
                data = it.content.map { it.toDto() },
                metadata = ResponseMetadata(
                    pagination = it.toPagination()
                )
            )
        });
    }

    @GetMapping("/images/{id}")
    fun getImage(@PathVariable id: Int): ResponseEntity<ImageDetails> {
        return imageService.getImage(id)
            .toDetailsDto()
            .let { ResponseEntity.ok(it) }
    }

    @DeleteMapping("/images/{id}")
    fun deleteImage(@PathVariable id: Int): ResponseEntity<Void> {
        imageService.deleteImage(id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/images/tag/{id}")
    fun deleteImageTag(@PathVariable id: Int): ResponseEntity<Void> {
        imageService.deleteImageTag(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/images/{id}/log")
    fun getImageLog(@PathVariable id: Int): ImageLogDto {
        return imageService.getImageLog(id)
            ?.let { ImageLogDto(data = it.data) } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}
