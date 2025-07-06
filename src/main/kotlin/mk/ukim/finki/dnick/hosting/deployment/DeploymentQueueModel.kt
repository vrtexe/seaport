package mk.ukim.finki.dnick.hosting.deployment

import mk.ukim.finki.dnick.hosting.model.domain.DeploymentState
import mk.ukim.finki.dnick.hosting.model.domain.PartialDeployment
import org.springframework.web.socket.WebSocketSession
import java.util.*

data class Deployment(
    val uid: UUID,
    val data: PartialDeployment,
    var status: DeploymentState,
    val statusListener: MutableMap<String, WebSocketSession> = mutableMapOf()
)

