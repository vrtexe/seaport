package mk.ukim.finki.dnick.hosting.deployment.log

import mk.ukim.finki.dnick.hosting.model.domain.Deployment
import mk.ukim.finki.dnick.hosting.model.domain.PartialDeployment
import org.springframework.web.socket.WebSocketSession
import java.util.*

data class Deployment(
    val uid: UUID,
    val data: Deployment,
    val log: StringBuilder = StringBuilder(),
    var watcher: AutoCloseable? = null,
    val logListener: MutableMap<String, WebSocketSession> = mutableMapOf()
)

