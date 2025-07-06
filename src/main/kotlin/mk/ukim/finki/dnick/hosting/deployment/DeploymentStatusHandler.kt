package mk.ukim.finki.dnick.hosting.deployment

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import mk.ukim.finki.dnick.hosting.model.domain.DeploymentState
import mk.ukim.finki.dnick.hosting.repository.DeploymentRepository
import mk.ukim.finki.dnick.hosting.service.DeploymentService
import mk.ukim.finki.dnick.hosting.socket.DeploymentDoneEventPublisher
import mk.ukim.finki.dnick.hosting.socket.StatusListenerAttachedEvent
import mk.ukim.finki.dnick.hosting.socket.StatusListenerAttachedEventPublisher
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
    private val deploymentService: DeploymentService,
    private val deploymentStatusCache: DeploymentStatusCache,
    private val statusListenerAttachedEventPublisher: StatusListenerAttachedEventPublisher
) {

    @Transactional(readOnly = true)
    fun subscribe(uid: UUID, socket: WebSocketSession) {
        deploymentStatusCache.getDeployment(uid)
            ?.let { addStatusListener(it, socket) }
            ?: deploymentService.fetchDeploymentData(uid)?.let {
                val deployment = Deployment(uid = uid, data = it, status = it.deployment.state)
                deploymentStatusCache.queue(deployment)
                addStatusListener(deployment, socket)
            }
    }

    private fun addStatusListener(deployment: Deployment, socket: WebSocketSession) {
        deploymentStatusCache.getDeployment(deployment.uid)?.let {
            it.statusListener[socket.id] = socket
            sendStatus(it, socket)
            statusListenerAttachedEventPublisher.publish(
                StatusListenerAttachedEvent(
                    namespace = it.data.namespace,
                    deploymentUid = it.uid,
                    deploymentName = it.data.deployment.name
                )
            )
        }
    }

    fun unsubscribe(imageUid: UUID, socket: WebSocketSession) {
        deploymentStatusCache.getDeployment(imageUid)?.statusListener?.let {
            it.remove(socket.id)
            if (it.isEmpty()) deploymentStatusCache.remove(imageUid)
        }
    }

    fun updateStatus(status: DeploymentState, uid: UUID) {
        deploymentStatusCache.getDeployment(uid)?.let {
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
                DeploymentState.STARTED -> handleDeploymentComplete(build)
                DeploymentState.FAILED -> handleDeploymentComplete(build)
                DeploymentState.STOPPED -> null
                DeploymentState.INITIAL -> null
            }
        }
    }

    private fun handleDeploymentComplete(data: Deployment) {
        cleanup(data.uid)
        deploymentDoneEventPublisher.publish()
    }

    private fun cleanup(uid: UUID) {
        deploymentCache.remove(uid)
        deploymentStatusCache.remove(uid)
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