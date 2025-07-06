package mk.ukim.finki.dnick.hosting.socket

import java.util.*

data class DeploymentStartedEvent(
    val namespace: String,
    val deploymentUid: UUID,
    val deploymentName: String
)