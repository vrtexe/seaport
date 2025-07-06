package mk.ukim.finki.dnick.hosting.builder

import io.kubernetes.client.custom.IntOrString
import io.kubernetes.client.openapi.models.*
import mk.ukim.finki.dnick.hosting.model.domain.*

private val K8S_INVALID_CHARACTERS_REGEX_START = "^[^a-zA-Z0-9]+".toRegex()
private val K8S_INVALID_CHARACTERS_REGEX_END = "[^a-zA-Z0-9]+$".toRegex()
private val K8S_INVALID_CHARACTERS_REGEX = "[^a-zA-Z0-9.]+".toRegex()

fun buildNamespaceDomain(namespace: String) = V1Namespace()
    .apiVersion("${ApiVersion.CORE_V1}")
    .kind("${Kind.NAMESPACE}")
    .metadata(
        V1ObjectMeta()
            .name(namespace)
            .labels(
                mapOf(
                    RESOURCE_NAME to namespace
                )
            )
    )

fun buildDeploymentDomain(namespace: String, deployment: Deployment, environment: Map<String, String>) = V1Deployment()
    .apiVersion("${ApiVersion.APPS_V1}")
    .metadata(
        V1ObjectMeta()
            .name(deployment.name)
            .namespace(namespace)
            .labels(
                mutableMapOf(
                    APP_LABEL to deployment.name,
                )
            )
    ).spec(
        V1DeploymentSpec()
            .replicas(
                when (deployment.state) {
                    DeploymentState.INITIAL -> 1
                    DeploymentState.STARTED -> 1
                    DeploymentState.STOPPED -> 0
                    DeploymentState.FAILED -> 0
                }
            )
            .selector(
                V1LabelSelector()
                    .matchLabels(
                        mutableMapOf(
                            APP_LABEL to deployment.name,
                        )
                    )
            )
            .template(
                V1PodTemplateSpec()
                    .metadata(
                        V1ObjectMeta().labels(
                            mutableMapOf(
                                APP_LABEL to deployment.name,
                            )
                        )
                    ).spec(
                        V1PodSpec()
                            .overhead(null)
                            .containers(
                                deployment.pods.map {
                                    V1Container()
                                        .name(it.name)
                                        .image("${INTERNAL_REGISTRY}/${it.imageTag.image.name}:${it.imageTag.version}")
                                        .ports(mutableListOf(V1ContainerPort().containerPort(it.port)))
                                        .env(
                                            mutableListOf(
                                                *environment.map { e ->
                                                    V1EnvVar()
                                                        .name(e.key)
                                                        .value(e.value)
                                                }.toTypedArray(),
                                                *it.environment.content.map { e ->
                                                    V1EnvVar()
                                                        .name(e.key)
                                                        .valueFrom(
                                                            V1EnvVarSource()
                                                                .configMapKeyRef(
                                                                    V1ConfigMapKeySelector()
                                                                        .name(it.environment.name)
                                                                        .key(e.key)
                                                                )
                                                        )
                                                }.toTypedArray()
                                            )
                                        )
                                }
                            )
                    )
            )
    )


fun buildServiceDomain(namespace: String, service: Service) = V1Service()
    .apiVersion("${ApiVersion.CORE_V1}")
    .kind("${Kind.SERVICE}")
    .metadata(
        V1ObjectMeta()
            .name(service.name)
            .namespace(namespace)
    )
    .spec(
        V1ServiceSpec()
            .selector(
                mutableMapOf(
                    APP_LABEL to service.deployment,
                )
            )
            .ports(
                service.ports.map {
                    V1ServicePort()
                        .port(it.port)
                        .targetPort(IntOrString(it.targetPort))
                }.toMutableList()
            )
    )


fun buildIngressDomain(namespace: String, data: Ingress) = V1Ingress()
    .apiVersion("${ApiVersion.NETWORKING_V1}")
    .kind("${Kind.INGRESS}")
    .metadata(
        V1ObjectMeta()
            .name(data.name)
            .namespace(namespace)
            .annotations(
                mutableMapOf(
                    "nginx.ingress.kubernetes.io/rewrite-target" to "/$2"
                )
            )
    )
    .spec(
        V1IngressSpec()
            .rules(
                data.rules.map {
                    V1IngressRule()
                        .http(
                            V1HTTPIngressRuleValue()
                                .paths(
                                    listOf(
                                        V1HTTPIngressPath()
                                            .pathType("Prefix")
                                            .path("/${namespace}/${it.path}(/|$)(.*)")
                                            .backend(
                                                V1IngressBackend()
                                                    .service(
                                                        V1IngressServiceBackend()
                                                            .name(it.servicePort.serviceName)
                                                            .port(
                                                                V1ServiceBackendPort()
                                                                    .number(it.servicePort.port)
                                                            )
                                                    )
                                            )
                                    )
                                )
                        )

                }.toMutableList()
            )
    )


fun buildConfigMapDomain(namespace: String, data: Environment) = V1ConfigMap()
    .apiVersion("${ApiVersion.CORE_V1}")
    .kind("${Kind.CONFIG_MAP}")
    .metadata(
        V1ObjectMeta()
            .name(data.name)
            .namespace(namespace)
    )
    .data(data.content.toMutableMap())


fun String.cleanupK8sName(): String {
    return this.lowercase().replace(K8S_INVALID_CHARACTERS_REGEX_START, "")
        .replace(K8S_INVALID_CHARACTERS_REGEX_END, "")
        .replace(K8S_INVALID_CHARACTERS_REGEX, "-")
}

private val INVALID_IMAGE_TAG_CHARACTERS = "[^a-z0-9_\\-./]+".toRegex()
private val INVALID_IMAGE_TAG_CHARACTERS_START = "^[^a-z0-9_\\-./]+".toRegex()
private val INVALID_IMAGE_TAG_CHARACTERS_END = "[^a-z0-9_\\-./]+$".toRegex()
private val COLLAPSE = "([\\-./])+".toRegex()

fun String.cleanupImageTag() = this.lowercase()
    .replace(INVALID_IMAGE_TAG_CHARACTERS_START, "")
    .replace(INVALID_IMAGE_TAG_CHARACTERS_END, "")
    .replace(INVALID_IMAGE_TAG_CHARACTERS, "-")
    .replace(COLLAPSE, "$1")