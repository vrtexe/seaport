package mk.ukim.finki.dnick.hosting.socket

import com.fasterxml.jackson.databind.ObjectMapper
import mk.ukim.finki.dnick.hosting.image.ImageBuildStatusHandler
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.*

@Component
class BuildStatusSocketHandler(
    private val jacksonObjectMapper: ObjectMapper,
    private val imageBuildStatusHandler: ImageBuildStatusHandler
) : TextWebSocketHandler() {

    private val socketSessions = mutableMapOf<String, MutableSet<UUID>>()

    data class BuildStatusMessage(
        val uid: String
    )

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        jacksonObjectMapper.readValue(message.payload, BuildStatusMessage::class.java)?.let {
            val uuid = UUID.fromString(it.uid)
            socketSessions.computeIfAbsent(session.id) { mutableSetOf() }.add(uuid)
            imageBuildStatusHandler.subscribe(uuid, session)
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        socketSessions[session.id] = mutableSetOf()
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        socketSessions.remove(session.id)?.let {
            for (imageUid in it) {
                imageBuildStatusHandler.unsubscribe(imageUid, session)
            }
        }
    }
}
