package mk.ukim.finki.dnick.hosting.service

import mk.ukim.finki.dnick.hosting.generated.model.GroupCreateRequest
import mk.ukim.finki.dnick.hosting.generated.model.GroupUpdateRequest
import mk.ukim.finki.dnick.hosting.model.entity.Application
import mk.ukim.finki.dnick.hosting.repository.ApplicationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class GroupService(
    private val applicationRepository: ApplicationRepository,
    private val namespaceService: NamespaceService
) {

    @Transactional(readOnly = true)
    fun getGroups(pageable: Pageable): Page<Application> {
        return applicationRepository.findAll(pageable)
    }

    @Transactional
    fun createGroup(request: GroupCreateRequest): Application {
        return applicationRepository.save(
            Application(
                name = request.name,
                namespace = namespaceService.resolveUserNamespace(),
            )
        )
    }

    @Transactional
    fun updateGroup(id: Int, request: GroupUpdateRequest): Application {
        return applicationRepository.findByIdOrNull(id)?.let {
            it.name = request.name
            applicationRepository.save(it)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found")
    }

    @Transactional
    fun deleteGroup(id: Int) {
        applicationRepository.findByIdOrNull(id)?.let {
            applicationRepository.delete(it)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found")
    }

}