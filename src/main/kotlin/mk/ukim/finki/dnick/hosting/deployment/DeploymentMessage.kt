package mk.ukim.finki.dnick.hosting.deployment


abstract class DefaultMessage(
    val type: MessageType
)

data class DeploymentLogMessage(
    val data: String,
) : DefaultMessage(MessageType.DEPLOYMENT_LOG)

data class DeploymentStatusMessage(
    val status: DeploymentLogStatusMessage,
) : DefaultMessage(MessageType.DEPLOYMENT_STATUS)

enum class DeploymentLogStatusMessage {
    INITIAL, STARTED, STOPPED, FAILED
}

enum class MessageType {
    DEPLOYMENT_LOG, DEPLOYMENT_STATUS
}