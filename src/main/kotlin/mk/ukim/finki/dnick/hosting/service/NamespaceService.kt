package mk.ukim.finki.dnick.hosting.service

import mk.ukim.finki.dnick.hosting.infra.config.auth.AuthenticationFacade
import mk.ukim.finki.dnick.hosting.model.domain.BaseNamespace
import mk.ukim.finki.dnick.hosting.model.entity.Namespace
import mk.ukim.finki.dnick.hosting.model.entity.User
import mk.ukim.finki.dnick.hosting.model.entity.toBaseDomain
import mk.ukim.finki.dnick.hosting.repository.NamespaceRepository
import mk.ukim.finki.dnick.hosting.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class NamespaceService(
    private val namespaceRepository: NamespaceRepository,
    private val authenticationFacade: AuthenticationFacade,
    private val userRepository: UserRepository
) {

    @Transactional
    fun getUserNamespace(): String {
        return resolveUserNamespace().name
    }

    @Transactional
    fun findUserNamespace(): BaseNamespace {
        return resolveUserNamespace().toBaseDomain()
    }

    @Transactional
    fun resolveUserNamespace(): Namespace {
        val user = authenticationFacade.user ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorised")
        val userId = UUID.fromString(user.id)

        return namespaceRepository.findByUserUid(userId)
            ?: createNamespace(userId)
    }

    private fun createNamespace(userId: UUID): Namespace {
        return namespaceRepository.save(
            Namespace(
                name = UUID.randomUUID().toString(),
                user = getUser(userId)
            )
        )
    }

    private fun getUser(uid: UUID): User {
        return userRepository.findByUid(uid)
            ?: userRepository.save(User(uid = uid))
    }
}