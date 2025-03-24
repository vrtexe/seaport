package mk.ukim.finki.dnick.hosting.service

import mk.ukim.finki.dnick.hosting.infra.config.UserProperties
import mk.ukim.finki.dnick.hosting.model.entity.Namespace
import mk.ukim.finki.dnick.hosting.repository.NamespaceRepository
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.transaction.annotation.Transactional

@Service
class NamespaceService(
    private val namespaceRepository: NamespaceRepository,
    private val userProperties: UserProperties
) {

    @Transactional(readOnly = true)
    fun getNamespace(name: String): Namespace {
        return namespaceRepository.findByName(name) ?: throw ResponseStatusException(
            NOT_FOUND,
            "Could not find namespace with name"
        )
    }

    @Transactional
    fun resolveUserNamespace(): Namespace {
        return namespaceRepository.findByName(userProperties.namespace)
            ?: namespaceRepository.save(Namespace(name = userProperties.namespace))
    }
}