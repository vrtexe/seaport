package mk.ukim.finki.dnick.hosting.infra.config

import mk.ukim.finki.dnick.hosting.socket.BuildLogSocketHandler
import mk.ukim.finki.dnick.hosting.socket.BuildSocketHandler
import mk.ukim.finki.dnick.hosting.socket.BuildStatusSocketHandler
import mk.ukim.finki.dnick.hosting.socket.StatusSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry


@Configuration
@EnableWebSocket
class WebsocketConfig(
    private val buildSocketHandler: BuildSocketHandler,
    private val statusSocketHandler: StatusSocketHandler,
    private val buildLogSocketHandler: BuildLogSocketHandler,
    private val buildStatusSocketHandler: BuildStatusSocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(buildSocketHandler, "/test").setAllowedOrigins("*")
        registry.addHandler(statusSocketHandler, "/status").setAllowedOrigins("*")
        registry.addHandler(buildLogSocketHandler, "/build/logs").setAllowedOrigins("*")
        registry.addHandler(buildStatusSocketHandler, "/build/status").setAllowedOrigins("*")
    }
}