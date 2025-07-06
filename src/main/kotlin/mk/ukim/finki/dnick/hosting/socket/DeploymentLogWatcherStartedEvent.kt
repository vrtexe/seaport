package mk.ukim.finki.dnick.hosting.socket

import java.util.*

data class DeploymentLogWatcherStartedEvent(
    val namespace: String,
    val deploymentUid: UUID,
    val deploymentName: String,
    val podName: String,
)