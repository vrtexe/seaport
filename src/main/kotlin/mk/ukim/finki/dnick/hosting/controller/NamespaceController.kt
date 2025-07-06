package mk.ukim.finki.dnick.hosting.controller

import mk.ukim.finki.dnick.hosting.model.dto.NamespaceDto
import mk.ukim.finki.dnick.hosting.model.dto.toDto
import mk.ukim.finki.dnick.hosting.service.ApplicationPersistenceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("$API_V1_PATH/namespace")
class NamespaceController(val applicationPersistenceService: ApplicationPersistenceService) {

    data class CreateNamespaceRequest(val name: String)

    @PostMapping
    fun createNamespace(@RequestBody body: CreateNamespaceRequest): ResponseEntity<Void> {
//        val namespace = applicationPersistenceService.createNamespace(body)

        return ResponseEntity.created(URI("$API_V1_PATH/namespace/${body.name}")).build()
    }

    @GetMapping("/{uid}")
    fun getNamespace(@PathVariable uid: String): NamespaceDto {
        return applicationPersistenceService.getNamespace(uid).toDto()
    }

}