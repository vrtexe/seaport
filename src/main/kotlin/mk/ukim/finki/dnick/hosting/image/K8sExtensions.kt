package mk.ukim.finki.dnick.hosting.image

import io.kubernetes.client.openapi.models.V1Pod

const val ErrorReason = "Error"
const val CompletedReason = "Completed"

fun V1Pod.getTerminatedStatus(): String? {
    return this.status?.containerStatuses?.firstOrNull()?.state?.terminated?.reason
}

fun V1Pod.isRunning(): Boolean {
    return this.status?.containerStatuses?.firstOrNull()?.state?.running?.startedAt != null
}

fun V1Pod.isReady(): Boolean {
    return this.status.conditions.find { it.type == "Ready" && it.status == "True" } != null
}
