package mk.ukim.finki.dnick.hosting.registry

import mk.ukim.finki.dnick.hosting.generated.registry.api.ManifestsApi.Companion.PATH_DELETE_IMAGE_MANIFEST
import mk.ukim.finki.dnick.hosting.generated.registry.api.ManifestsApi.Companion.PATH_GET_IMAGE_MANIFEST
import mk.ukim.finki.dnick.hosting.generated.registry.model.GetImageManifest200Response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.toEntity

@Component
class RegistryClient(private val client: WebClient) {

    @Autowired
    constructor(factory: RegistryClientFactory)
            : this(factory.createWebClient())

    fun getManifests(name: String, reference: String): ResponseEntity<GetImageManifest200Response>? {
        return client.get()
            .uri(PATH_GET_IMAGE_MANIFEST, name, reference)
            .retrieve()
            .toEntity<GetImageManifest200Response>()
            .block()
    }

    fun deleteManifest(name: String, reference: String): ResponseEntity<Void>? {
        return client.delete()
            .uri(PATH_DELETE_IMAGE_MANIFEST, name, reference)
            .retrieve()
            .toEntity<Void>()
            .block()
    }
}