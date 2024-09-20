package mk.ukim.finki.dnick.hosting.controller

data class ApplicationCreateRequest(
    val namespace: String,
    val name: String,
    val image: ImageRequest,
    val deployments: List<DeploymentRequest>
)

data class DeploymentRequest(
    val name: String,
    val pod: PodRequest,
    val service: ServiceRequest,
    val ingress: IngressRequest
)

data class IngressRequest(
    val path: String,
)

data class ServiceRequest(
    val name: String,
    val port: Int,
    val targetPort: Int
)

data class PodRequest(
    val name: String,
    val workdir: String,
    val image: ImageRequest,
    val environment: EnvironmentRequest,
)

data class ImageRequest(
    val name: String,
    val version: String,
)

data class EnvironmentRequest(
    val id: String?,
    val data: EnvironmentDataRequest?,
)

data class EnvironmentDataRequest(
    val name: String,
    val content: Map<String, String>
)

