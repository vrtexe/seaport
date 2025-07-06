package mk.ukim.finki.dnick.hosting.image

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import mk.ukim.finki.dnick.hosting.model.entity.ImageStatus
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.UUID

private val log = KotlinLogging.logger {}

@Component
class ImageBuildLogger(
    private val objectMapper: ObjectMapper,
    private val imageBuildCache: ImageBuildCache
) {

    fun subscribe(imageUid: UUID, socket: WebSocketSession) {
        imageBuildCache.getImageBuild(imageUid)?.let {
            it.logListener[socket.id] = socket
            sendText(it, socket)
        }
    }

    fun unsubscribe(imageUid: UUID, socket: WebSocketSession) {
        imageBuildCache.getImageBuild(imageUid)?.logListener?.remove(socket.id)
    }

    fun writeLog(text: String, imageUid: UUID) {
        imageBuildCache.getImageBuild(imageUid)?.let {
            it.log.appendLine(text)
            it.logListener.values.forEach { s ->
                sendPartialText(text, it, s)
            }
        }
    }

    private fun sendText(imageBuild: ImageBuild, socket: WebSocketSession) {
        sendPartialText(null, imageBuild, socket)
    }

    private fun sendPartialText(message: String?, imageBuild: ImageBuild, socket: WebSocketSession) {
        if (!socket.isOpen) {
            unsubscribe(imageBuild.uid, socket)
            return
        }

        toLogJson(imageBuild, message)?.let { socket.sendMessage(TextMessage(it)) }
    }

    private fun toLogJson(imageBuild: ImageBuild, data: String? = null): String? {
        try {
            return objectMapper.writeValueAsString(imageBuild.toMessageResponse(data))
        } catch (e: Exception) {
            log.error(e) { "Could not serialize json" }
        }
        return null
    }

    private fun ImageStatus.toMessageResponse() = ImageLogStatusMessage.valueOf(this.name.uppercase())
    private fun ImageBuild.toMessageResponse(override: String? = null) = ImageLogMessage(
        status = this.status.toMessageResponse(),
        data = override ?: this.log.toString()
    )

}

abstract class DefaultMessage(
    val type: MessageType
)

data class ImageLogMessage(
    val status: ImageLogStatusMessage,
    val data: String,
) : DefaultMessage(MessageType.IMAGE_LOG)

data class ImageStatusMessage(
    val status: ImageLogStatusMessage,
) : DefaultMessage(MessageType.IMAGE_STATUS)

enum class ImageLogStatusMessage {
    INITIALIZED, STARTED, COMPLETED, FAILED
}

enum class MessageType {
    IMAGE_LOG, IMAGE_STATUS
}