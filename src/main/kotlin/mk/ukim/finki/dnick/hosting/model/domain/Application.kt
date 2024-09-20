package mk.ukim.finki.dnick.hosting.model.domain

import java.util.*

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
    val name: String,
    val pods: Set<Pod>
)

data class Pod(
    val id: Int,
    val name: String,
    val port: Int,
    val workdir: String,
    val image: Image,
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
    var version: String,
    val hash: UUID,
    val baseRef: Int,
    val arguments: Map<String, String> = emptyMap(),
)

