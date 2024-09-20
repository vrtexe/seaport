package mk.ukim.finki.dnick.hosting.socket

import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
class SocketSessionCache {

    private val socketSessions: MutableMap<String, WebSocketSession> = ConcurrentHashMap()

    fun add(session: WebSocketSession) {
        this.socketSessions[session.id] = session
    }

    fun remove(session: WebSocketSession) {
        this.socketSessions.remove(session.id)
    }

    fun get(sessionId: String): WebSocketSession? {
        return socketSessions[sessionId]
    }
}