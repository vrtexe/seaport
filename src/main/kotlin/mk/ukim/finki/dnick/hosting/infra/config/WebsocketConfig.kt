package mk.ukim.finki.dnick.hosting.infra.config

import mk.ukim.finki.dnick.hosting.socket.*
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry


@Configuration
@EnableWebSocket
class WebsocketConfig(
    private val buildSocketHandler: BuildSocketHandler,
    private val buildLogSocketHandler: BuildLogSocketHandler,
    private val buildStatusSocketHandler: BuildStatusSocketHandler,
    private val deploymentLogSocketHandler: DeploymentLogSocketHandler,
    private val deploymentStatusSocketHandler: DeploymentStatusSocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(buildLogSocketHandler, "/build/logs").setAllowedOrigins("*")
        registry.addHandler(buildStatusSocketHandler, "/build/status").setAllowedOrigins("*")

        registry.addHandler(deploymentLogSocketHandler, "/deployment/logs").setAllowedOrigins("*")
        registry.addHandler(deploymentStatusSocketHandler, "/deployment/status").setAllowedOrigins("*")
    }
}