package mk.ukim.finki.dnick.hosting.socket

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class StatusSocketHandler(
    private val statusSocketSessionCache: StatusSocketSessionCache,
    private val jacksonObjectMapper: ObjectMapper
) : TextWebSocketHandler() {

    data class DeploymentMessage(
        val ids: Set<Int>
    )

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        jacksonObjectMapper.readValue(message.payload, DeploymentMessage::class.java)
            ?.let {
                statusSocketSessionCache.subscribe(session, it.ids)
            }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        statusSocketSessionCache.add(session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        statusSocketSessionCache.remove(session)
    }
}
