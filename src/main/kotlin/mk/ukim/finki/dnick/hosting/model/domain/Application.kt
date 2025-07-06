package mk.ukim.finki.dnick.hosting.model.domain

import java.util.*
import java.util.stream.Collectors

data class Application(
    val id: Int,
    val name: String,
    val namespace: String,

    val deployments: Set<Deployment>,
    val ingresses: Set<Ingress>,
    val services: Set<Service>,
    val images: Set<Image>
)

data class Ingress(
    val id: Int,
    val name: String,
    val rules: Set<IngressRule>
)

data class IngressRule(
    val id: Int,
    val path: String,
    val servicePort: ServicePort,
)

data class ServicePort(
    val id: Int,
    val name: String,
    val port: Int,
    val targetPort: Int,
    val serviceName: String,
)

data class ServiceDeployment(
    val name: String,
    val port: Int
)

data class Deployment(
    val id: Int,
    val uid: String,
    val name: String,
    val state: DeploymentState,
    val namespace: String,
    val pods: Set<Pod>
)

data class PartialDeployment(
    val namespace: String,
    val deployment: Deployment,
    val service: Service,
    val ingress: Ingress?,
)

enum class DeploymentState(private val value: String) {
    INITIAL("INITIAL"),
    STARTED("STARTED"),
    STOPPED("STOPPED"),
    FAILED("FAILED");

    companion object {
        @JvmStatic
        val VALUE_MAP: Map<String, DeploymentState> = entries.stream()
            .collect(Collectors.toMap({ it.value }, { it }));

        fun of(str: String?): DeploymentState? = VALUE_MAP.getOrDefault(str?.uppercase(), null)
    }
}

data class Pod(
    val id: Int,
    val name: String,
    val port: Int,
    val workdir: String,
    val imageTag: ImageTag,
    val environment: Environment,
)

data class Service(
    val id: Int,
    val name: String,
    val deployment: String,
    val ports: Set<ServicePort>
)

data class Image(
    val id: Int,
    val name: String,
)

data class ImageTag(
    val id: Int,
    var version: String,
    val hash: UUID,
    val arguments: Map<String, String>,
    val image: Image,
    val baseRef: Int,
)


