package mk.ukim.finki.dnick.hosting.deployment.log

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import mk.ukim.finki.dnick.hosting.deployment.DeploymentLogMessage
import mk.ukim.finki.dnick.hosting.model.entity.toDomain
import mk.ukim.finki.dnick.hosting.repository.DeploymentRepository
import mk.ukim.finki.dnick.hosting.socket.DeploymentLogWatcherStartedEvent
import mk.ukim.finki.dnick.hosting.socket.DeploymentLogWatcherStartedEventPublisher
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.*

private val log = KotlinLogging.logger {}

@Component
class DeploymentLogger(
    private val objectMapper: ObjectMapper,
    private val deploymentCache: DeploymentLogCache,
    private val deploymentRepository: DeploymentRepository,
    private val deploymentLogWatcherStartedEventPublisher: DeploymentLogWatcherStartedEventPublisher
) {

    @Transactional
    fun subscribe(uid: UUID, socket: WebSocketSession) {
        deploymentCache.getDeployment(uid)
            ?.let { addLogListener(it, socket) }
            ?: deploymentRepository.findByUid(uid)?.let {
                val deployment = Deployment(uid = it.uid, data = it.toDomain())
                deploymentCache.queue(deployment)
                addLogListener(deployment, socket)
                deploymentLogWatcherStartedEventPublisher.publish(
                    DeploymentLogWatcherStartedEvent(
                        namespace = it.application.namespace.name,
                        deploymentUid = it.uid,
                        deploymentName = it.name,
                        podName = it.pods.firstOrNull()?.name!!
                    )
                )
            }
    }

    private fun addLogListener(deployment: Deployment, socket: WebSocketSession) {
        deployment.logListener[socket.id] = socket
        sendText(deployment, socket)
    }

    fun unsubscribe(uid: UUID, socket: WebSocketSession) {
        deploymentCache.getDeployment(uid)?.let {
            it.logListener.remove(socket.id)
            if (it.logListener.isEmpty()) {
                try {
                    it.watcher?.close();
                } catch (_: Exception) {
                }
                deploymentCache.dequeue(uid)
            }
        }
    }

    fun writeLog(text: String, uid: UUID) {
        deploymentCache.getDeployment(uid)?.let {
            it.log.appendLine(text)
            it.logListener.values.forEach { s ->
                sendPartialText(text, it, s)
            }
        }
    }

    private fun sendText(deployment: Deployment, socket: WebSocketSession) {
        sendPartialText(null, deployment, socket)
    }

    private fun sendPartialText(message: String?, deployment: Deployment, socket: WebSocketSession) {
        if (!socket.isOpen) {
            unsubscribe(deployment.uid, socket)
            return
        }

        toLogJson(deployment, message)?.let { socket.sendMessage(TextMessage(it)) }
    }

    private fun toLogJson(deployment: Deployment, data: String? = null): String? {
        try {
            return objectMapper.writeValueAsString(deployment.toMessageResponse(data))
        } catch (e: Exception) {
            log.error(e) { "Could not serialize json" }
        }
        return null
    }

    private fun Deployment.toMessageResponse(override: String? = null) = DeploymentLogMessage(
        data = override ?: this.log.toString()
    )
}

