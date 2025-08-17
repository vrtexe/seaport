package mk.ukim.finki.dnick.hosting.controller

import jakarta.validation.Valid
import mk.ukim.finki.dnick.hosting.generated.model.*
import mk.ukim.finki.dnick.hosting.model.domain.BaseImageArgStage
import mk.ukim.finki.dnick.hosting.model.domain.BaseImageArgType
import mk.ukim.finki.dnick.hosting.service.BaseImageArgumentRequest
import mk.ukim.finki.dnick.hosting.service.BaseImageService
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.stream.Stream
import mk.ukim.finki.dnick.hosting.generated.model.BaseImageRequest as BaseImageRequestDto

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("$API_V2_PATH/base-image")
class BaseImageController(private val baseImageService: BaseImageService) {

    @GetMapping
    fun getBaseImages(
        @Valid baseImageCriteria: BaseImageCriteria,
        @Valid pageable: Pageable = Pageable.unpaged()
    ): ResponseEntity<BaseImageResponse> {
        return baseImageService.findBaseImagesBy(baseImageCriteria, pageable)
            .let {
                BaseImageResponse(
                    data = it.flatMap { b ->
                        Stream.concat(
                            if ((baseImageCriteria.type == null || baseImageCriteria.type == BaseImageType.EXE) && baseImageCriteria.buildTool == null)
                                b.baseImagesExe.stream().map { be ->
                                    BaseImage(
                                        id = be.ref.id!!,
                                        type = BaseImageType.EXE,
                                        language = b.language,
                                        version = b.version,
                                        arguments = be.ref.arguments.map { a ->
                                            BaseImageArgument(
                                                name = a.name,
                                                description = a.description,
                                                type = BaseArgumentType.forValue(a.type.name.uppercase()),
                                                stage = BaseArgumentStage.forValue(a.stage.name.uppercase())
                                            )
                                        }
                                    )
                                } else Stream.empty(),
                            if (baseImageCriteria.type == null || baseImageCriteria.type == BaseImageType.GIT)
                                b.baseImagesGit.stream()
                                    .filter { bg ->
                                        baseImageCriteria.buildTool == null ||
                                                bg.buildTool.contains(baseImageCriteria.buildTool)
                                    }
                                    .map { bg ->
                                        BaseImage(
                                            id = bg.ref.id!!,
                                            type = BaseImageType.GIT,
                                            language = b.language,
                                            version = b.version,
                                            arguments = bg.ref.arguments.map { a ->
                                                BaseImageArgument(
                                                    name = a.name,
                                                    description = a.description,
                                                    type = BaseArgumentType.forValue(a.type.name.uppercase()),
                                                    stage = BaseArgumentStage.forValue(a.stage.name.uppercase())
                                                )
                                            }
                                        )
                                    }
                            else Stream.empty()
                        )
                    }.toList(),
                    metadata = ResponseMetadata(it.toPagination())
                )
            }.let { ResponseEntity.ok(it) }
    }

    @PostMapping
    fun createBaseImage(@RequestBody body: BaseImageRequestDto) {
        baseImageService.createBaseImage(body)
    }

    @PutMapping("/{id}")
    fun updateBaseImage(@PathVariable id: Int, @RequestBody body: BaseImageUpdateRequest) {
        baseImageService.updateBaseImage(id, body)
    }

    @DeleteMapping("/{id}")
    fun deleteBaseImage(@PathVariable id: Int) {
        baseImageService.deleteBaseImage(id)
    }

    @GetMapping("/languages")
    fun getLanguages(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(baseImageService.findLanguages())
    }

    @GetMapping("/language/{name}/versions")
    fun getLanguageVersions(@PathVariable name: String): ResponseEntity<List<String>> {
        return ResponseEntity.ok(baseImageService.findLanguageVersions(name))
    }

    @GetMapping("/build-tools")
    fun getBuildTools(
        @RequestParam("language") language: String,
        @RequestParam("version") version: String
    ): ResponseEntity<List<String>> {
        return ResponseEntity.ok(baseImageService.findImageBuildTools(language, version))
    }

    @GetMapping("/build-tool/{name}/versions")
    fun getBuildTools(@PathVariable name: String): ResponseEntity<List<String>> {
        return ResponseEntity.ok(baseImageService.findBuildToolVersions(name))
    }

    data class BaseImageArgumentsResponse(
        val arguments: List<BaseImageArgumentDto>,
    )

    data class BaseImageArgumentDto(
        val name: String,
        val description: String,
        val type: BaseImageArgType,
        val stage: BaseImageArgStage
    )

    @GetMapping("/build-arguments")
    fun getBuildArguments(buildTool: BaseImageArgumentRequest): ResponseEntity<BaseImageArgumentsResponse> {
        return BaseImageArgumentsResponse(
            arguments = baseImageService.findArguments(buildTool).map {
                BaseImageArgumentDto(
                    name = it.name,
                    description = it.description,
                    type = it.type,
                    stage = it.stage
                )
            }
        ).let { ResponseEntity.ok(it) }
    }

}