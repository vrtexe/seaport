package mk.ukim.finki.dnick.hosting.infra.config

import mk.ukim.finki.dnick.hosting.socket.TestSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry


@Configuration
@EnableWebSocket
class WebsocketConfig(private val testSocketHandler: TestSocketHandler) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(testSocketHandler, "/test").setAllowedOrigins("*")
    }
}