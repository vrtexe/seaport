package mk.ukim.finki.dnick.hosting.controller

import mk.ukim.finki.dnick.hosting.model.domain.BaseImageArgStage
import mk.ukim.finki.dnick.hosting.model.domain.BaseImageArgType
import mk.ukim.finki.dnick.hosting.service.BaseImageRequest
import mk.ukim.finki.dnick.hosting.service.BaseImageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("$API_V1_PATH/base-image")
class BaseImageController(private val baseImageService: BaseImageService) {

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
    fun getBuildArguments(buildTool: BaseImageRequest): ResponseEntity<BaseImageArgumentsResponse> {
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