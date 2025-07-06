package mk.ukim.finki.dnick.hosting.deployment

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import mk.ukim.finki.dnick.hosting.model.domain.DeploymentState
import mk.ukim.finki.dnick.hosting.repository.DeploymentRepository
import mk.ukim.finki.dnick.hosting.service.DeploymentService
import mk.ukim.finki.dnick.hosting.socket.DeploymentDoneEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.*
import mk.ukim.finki.dnick.hosting.model.entity.Deployment as DeploymentEntity
import mk.ukim.finki.dnick.hosting.model.entity.DeploymentState as DeploymentStateEntity

private val log = KotlinLogging.logger {}

@Component
class DeploymentStatusHandler(
    private val objectMapper: ObjectMapper,
    private val deploymentCache: DeploymentCache,
    private val deploymentRepository: DeploymentRepository,
    private val deploymentDoneEventPublisher: DeploymentDoneEventPublisher,
    private val deploymentService: DeploymentService
) {

    @Transactional(readOnly = true)
    fun subscribe(uid: UUID, socket: WebSocketSession) {
        deploymentCache.getDeployment(uid)
            ?.let { addLogListener(it, socket) }
            ?: deploymentService.fetchDeploymentData(uid)?.let {
                val deployment = Deployment(uid = uid, data = it, status = it.deployment.state)
                deploymentCache.queue(deployment)
                addLogListener(deployment, socket)
            }
    }

    private fun addLogListener(deployment: Deployment, socket: WebSocketSession) {
        deployment.statusListener[socket.id] = socket
        sendStatus(deployment, socket)
    }

    fun unsubscribe(imageUid: UUID, socket: WebSocketSession) {
        deploymentCache.getDeployment(imageUid)?.statusListener?.remove(socket.id)
    }

    fun updateStatus(status: DeploymentState, imageUid: UUID) {
        deploymentCache.getDeployment(imageUid)?.let {
            it.status = status
            handleStatus(status, it)
            it.statusListener.values.forEach { socket ->
                sendStatus(it, socket)
            }
        }
    }

    private fun handleStatus(status: DeploymentState, build: Deployment) {
        saveStatus(status, build.uid)?.let {
            when (status) {
                DeploymentState.STARTED -> handleBuildComplete(build)
                DeploymentState.FAILED -> handleBuildComplete(build)
                DeploymentState.STOPPED -> null
                DeploymentState.INITIAL -> null
            }
        }
    }

    private fun handleBuildComplete(data: Deployment) {
        cleanupBuild(data.uid)
        deploymentDoneEventPublisher.publish()
    }

    private fun cleanupBuild(imageUid: UUID) {
        deploymentCache.removeBuild(imageUid)
    }

    private fun saveStatus(
        status: DeploymentState,
        uid: UUID
    ): DeploymentEntity? {
        return deploymentRepository.findByUid(uid)?.let {
            it.state = DeploymentStateEntity.valueOf(status.name.lowercase())
            deploymentRepository.save(it)
        }

    }

    private fun sendStatus(deployment: Deployment, socket: WebSocketSession) {
        try {
            toStatusJson(deployment)?.let { socket.sendMessage(TextMessage(it)) }
        } catch (error: RuntimeException) {
            log.error(error) { "Could not send message" }
        }
    }


    private fun toStatusJson(deployment: Deployment): String? {
        try {
            return objectMapper.writeValueAsString(deployment.toStatusMessageResponse())
        } catch (e: Exception) {
            log.error(e) { "Could not serialize json" }
        }
        return null
    }

    fun DeploymentState.toMessageResponse() = DeploymentLogStatusMessage.valueOf(this.name.uppercase())
    fun Deployment.toStatusMessageResponse() = DeploymentStatusMessage(
        status = this.status.toMessageResponse(),
    )
}