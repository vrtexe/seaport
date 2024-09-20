package mk.ukim.finki.dnick.hosting.builder

import io.kubernetes.client.custom.IntOrString
import io.kubernetes.client.openapi.models.*
import mk.ukim.finki.dnick.hosting.image.ImageData
import mk.ukim.finki.dnick.hosting.service.ApplicationDeploymentService


fun buildNamespace(namespace: String) = V1Namespace()
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

fun buildPod(pod: ApplicationDeploymentService.PodData) = V1Pod()
    .apiVersion("${ApiVersion.CORE_V1}")
    .kind("${Kind.POD}")
    .metadata(
        V1ObjectMeta()
            .name(pod.name)
            .namespace(pod.namespace)
    )
    .spec(
        V1PodSpec()
            .overhead(null)
            .containers(
                listOf(
                    V1Container()
                        .name("${pod.name}-${CONTAINER_SUFFIX}")
                        .image("${INTERNAL_REGISTRY}/${pod.image.name}:${pod.image.version}")
                )
            )
    )


data class EnvironmentData(
    val values: List<EnvironmentValueData>,
    val configMapValues: List<EnvironmentConfigMapValueData>
)

data class EnvironmentValueData(
    val name: String,
    val value: String
)

data class EnvironmentConfigMapValueData(
    val name: String,
    val configMap: String
)


data class DeploymentData(
    val name: String,
    val port: Int,
    val namespace: String,
    val environment: EnvironmentData,
    val image: ImageData
)

fun buildDeployment(deployment: DeploymentData) = V1Deployment()
    .apiVersion("${ApiVersion.APPS_V1}")
    .metadata(
        V1ObjectMeta()
            .name(deployment.name)
            .namespace(deployment.namespace)
            .labels(
                mutableMapOf(
                    APP_LABEL to deployment.name,
                )
            )
    ).spec(
        V1DeploymentSpec()
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
                                listOf(
                                    V1Container()
                                        .name("${deployment.name}-${CONTAINER_SUFFIX}")
                                        .image("${INTERNAL_REGISTRY}/${deployment.image.name}:${deployment.image.version}")
                                        .ports(mutableListOf(V1ContainerPort().containerPort(deployment.port)))
                                        .env(
                                            mutableListOf(
                                                *deployment.environment.values.map {
                                                    V1EnvVar()
                                                        .name(it.name)
                                                        .value(it.value)
                                                }.toTypedArray(),
                                                *deployment.environment.configMapValues.map {
                                                    V1EnvVar()
                                                        .name(it.name)
                                                        .valueFrom(
                                                            V1EnvVarSource()
                                                                .configMapKeyRef(
                                                                    V1ConfigMapKeySelector()
                                                                        .name(it.configMap)
                                                                        .key(it.name)
                                                                )
                                                        )
                                                }.toTypedArray()
                                            )
                                        )
                                )
                            )
                    )
            )
    )

data class K8sServiceData(
    val name: String,
    val namespace: String,
    val port: Int,
    val deployment: K8sServiceDeploymentData,
)

data class K8sServiceDeploymentData(
    val name: String,
    val targetPort: Int,
)

fun buildService(service: K8sServiceData) = V1Service()
    .apiVersion("${ApiVersion.CORE_V1}")
    .kind("${Kind.SERVICE}")
    .metadata(
        V1ObjectMeta()
            .name(service.name)
            .namespace(service.namespace)
    )
    .spec(
        V1ServiceSpec()
            .selector(
                mutableMapOf(
                    APP_LABEL to service.deployment.name,
                )
            )
            .ports(
                mutableListOf(
                    V1ServicePort()
                        .port(service.port)
                        .targetPort(IntOrString(service.deployment.targetPort))
                )
            )
    )


data class IngressDataBuild(
    val name: String,
    val deployment: String,
    val namespace: String,
    val service: IngressServiceData
)

data class IngressServiceData(
    val name: String,
    val port: Int
)

fun buildIngress(data: IngressDataBuild) = V1Ingress()
    .apiVersion("${ApiVersion.NETWORKING_V1}")
    .kind("${Kind.INGRESS}")
    .metadata(
        V1ObjectMeta()
            .name(data.name)
            .namespace(data.namespace)
            .annotations(
                mutableMapOf(
                    "nginx.ingress.kubernetes.io/rewrite-target" to "/$2"
                )
            )
    )
    .spec(
        V1IngressSpec()
            .rules(
                mutableListOf(
                    V1IngressRule()
                        .http(
                            V1HTTPIngressRuleValue()
                                .paths(
                                    listOf(
                                        V1HTTPIngressPath()
                                            .pathType("Prefix")
                                            .path("/${data.namespace}/${data.deployment}(/|\\$)(.*)")
                                            .backend(
                                                V1IngressBackend()
                                                    .service(
                                                        V1IngressServiceBackend()
                                                            .name(data.service.name)
                                                            .port(
                                                                V1ServiceBackendPort()
                                                                    .number(data.service.port)
                                                            )
                                                    )
                                            )
                                    )
                                )
                        )
                )
            )
    )

data class ConfigMapData(
    val name: String,
    val namespace: String,
    val values: Map<String, String>
)

fun buildConfigMap(data: ConfigMapData) = V1ConfigMap()
    .apiVersion("${ApiVersion.CORE_V1}")
    .kind("${Kind.CONFIG_MAP}")
    .metadata(
        V1ObjectMeta()
            .name(data.name)
            .namespace(data.namespace)
    )
    .data(data.values.toMutableMap())

