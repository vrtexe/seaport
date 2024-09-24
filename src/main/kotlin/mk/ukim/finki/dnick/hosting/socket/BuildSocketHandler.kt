package mk.ukim.finki.dnick.hosting.socket

import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class BuildSocketHandler(private val socketSessionCache: SocketSessionCache) : TextWebSocketHandler() {

    override fun afterConnectionEstablished(session: WebSocketSession) {
        socketSessionCache.add(session)
        session.sendMessage(
            TextMessage(
                """
                {
                    "type": "IDENTIFIER",
                    "id": "${session.id}"
                }
                """.trimIndent()
            )
        )
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        socketSessionCache.remove(session)
    }
}