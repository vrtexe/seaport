package mk.ukim.finki.dnick.hosting.image

import io.kubernetes.client.openapi.models.V1Pod

const val ErrorExitCode = 1
const val ErrorReason = "Error"
const val CompletedReason = "Completed"

fun V1Pod.getTerminatedStatus(): String? {
    return this.status?.containerStatuses?.first()?.state?.terminated?.reason
}

fun V1Pod.getTerminatedExitCode(): Int? {
    return this.status?.containerStatuses?.first()?.state?.terminated?.exitCode
}