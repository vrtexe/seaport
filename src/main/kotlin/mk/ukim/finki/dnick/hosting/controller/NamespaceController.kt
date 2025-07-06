package mk.ukim.finki.dnick.hosting.controller

import mk.ukim.finki.dnick.hosting.generated.api.NamespaceApi
import mk.ukim.finki.dnick.hosting.generated.model.Namespace
import mk.ukim.finki.dnick.hosting.mapper.toResponse
import mk.ukim.finki.dnick.hosting.service.NamespaceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class NamespaceController(private val namespaceService: NamespaceService) : NamespaceApi {

//    @GetMapping("/v2/namespace/user")
    override fun getNamespace(): ResponseEntity<Namespace> {
        return ResponseEntity.ok(namespaceService.findUserNamespace().toResponse())
    }
}