package mk.ukim.finki.dnick.hosting.infra.config

import mk.ukim.finki.dnick.hosting.socket.BuildLogSocketHandler
import mk.ukim.finki.dnick.hosting.socket.BuildStatusSocketHandler
import mk.ukim.finki.dnick.hosting.socket.DeploymentLogSocketHandler
import mk.ukim.finki.dnick.hosting.socket.DeploymentStatusSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry


@Configuration
@EnableWebSocket
class WebsocketConfig(
    private val buildLogSocketHandler: BuildLogSocketHandler,
    private val buildStatusSocketHandler: BuildStatusSocketHandler,
    private val deploymentLogSocketHandler: DeploymentLogSocketHandler,
    private val deploymentStatusSocketHandler: DeploymentStatusSocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(buildLogSocketHandler, "/ws/build/logs").setAllowedOrigins("*")
        registry.addHandler(buildStatusSocketHandler, "/ws/build/status").setAllowedOrigins("*")
        registry.addHandler(deploymentLogSocketHandler, "/ws/deployment/logs").setAllowedOrigins("*")
        registry.addHandler(deploymentStatusSocketHandler, "/ws/deployment/status").setAllowedOrigins("*")
    }
}