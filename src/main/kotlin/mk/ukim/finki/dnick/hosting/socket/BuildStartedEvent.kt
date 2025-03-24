package mk.ukim.finki.dnick.hosting.socket

import java.util.UUID

data class BuildStartedEvent(
    val imageKey: String,
    val jobName: String,
    val namespace: String,
    val imageUid: UUID
)