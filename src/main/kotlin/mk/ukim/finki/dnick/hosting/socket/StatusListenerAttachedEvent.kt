package mk.ukim.finki.dnick.hosting.socket

import java.util.*

data class StatusListenerAttachedEvent(
    val namespace: String,
    val deploymentUid: UUID,
    val deploymentName: String
)