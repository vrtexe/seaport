package mk.ukim.finki.dnick.hosting.service

import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.JSON
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.apis.NetworkingV1Api
import io.kubernetes.client.openapi.models.V1ConfigMapKeySelector
import io.kubernetes.client.openapi.models.V1Deployment
import io.kubernetes.client.openapi.models.V1EnvVar
import io.kubernetes.client.openapi.models.V1EnvVarSource
import io.kubernetes.client.util.PatchUtils
import mk.ukim.finki.dnick.hosting.builder.*
import mk.ukim.finki.dnick.hosting.controller.ApplicationController
import mk.ukim.finki.dnick.hosting.image.ImageData
import mk.ukim.finki.dnick.hosting.model.domain.Deployment
import mk.ukim.finki.dnick.hosting.model.domain.Environment
import mk.ukim.finki.dnick.hosting.model.domain.Ingress
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import mk.ukim.finki.dnick.hosting.model.domain.Service as DomainService


@Service
class ApplicationDeploymentService(
    private val coreV1Api: CoreV1Api,
    private val appsV1Api: AppsV1Api,
    private val client: ApiClient,
    private val networkingV1Api: NetworkingV1Api
) {

    companion object {
        const val STOP_REPLICAS = 0
        const val START_REPLICAS = 1
    }

    fun createNamespace(namespace: String) {
        if (namespaceExists(namespace)) {
            return
        }

        val namespaceV1 = buildNamespace(namespace)
        coreV1Api.createNamespace(namespaceV1).execute()
    }

    fun stopDeployment(name: String, namespace: String) {
        if (!deploymentExists(name, namespace)) {
            return
        }

        patchReplicas(name, namespace, STOP_REPLICAS)
    }

    fun startDeployment(name: String, namespace: String) {
        if (!deploymentExists(name, namespace)) {
            return
        }

        patchReplicas(name, namespace, START_REPLICAS)
    }

    private fun patchReplicas(name: String, namespace: String, replicas: Int) {
        PatchUtils.patch(
            V1Deployment::class.java,
            {
                appsV1Api.patchNamespacedDeployment(
                    // language=json
                    name, namespace, V1Patch("""{ "spec": { "replicas": $replicas } }""")
                ).pretty("true").buildCall(null)
            },
            V1Patch.PATCH_FORMAT_STRATEGIC_MERGE_PATCH,
            client
        )
    }

    fun patchImageVersion(pod: String, image: ImageData, name: String, namespace: String) {
        PatchUtils.patch(
            V1Deployment::class.java,
            {
                appsV1Api.patchNamespacedDeployment(
                    name, namespace, V1Patch(
                        // language=json
                        """
                        |  {
                        |    "spec": {
                        |      "template": {
                        |        "spec": {
                        |          "containers": [
                        |            {
                        |              "name": "$pod",
                        |              "image": "internal.io/${image.name}:${image.version}"
                        |            }
                        |          ]
                        |        }
                        |      }
                        |    }
                        | }""".trimMargin()
                    )
                ).pretty("true").buildCall(null)
            },
            V1Patch.PATCH_FORMAT_STRATEGIC_MERGE_PATCH,
            client
        )
    }

    data class PodData(
        val name: String,
        val namespace: String,
        val image: ImageData
    )

    fun createService(data: K8sServiceData) {
        if (serviceExists(data.name, data.namespace)) {
            coreV1Api.deleteNamespacedService(data.name, data.namespace).execute()
        }

        coreV1Api.createNamespacedService(data.namespace, buildService(data)).execute()
    }

    fun createService(namespace: String, data: DomainService) {
        if (serviceExists(data.name, namespace)) {
            coreV1Api.deleteNamespacedService(data.name, namespace).execute()
        }

        coreV1Api.createNamespacedService(namespace, buildServiceDomain(namespace, data)).execute()
    }


    fun createIngress(data: IngressDataBuild) {
        if (ingressExists(data.name, data.namespace)) {
            networkingV1Api.deleteNamespacedIngress(data.name, data.namespace).execute()
        }

        networkingV1Api.createNamespacedIngress(data.namespace, buildIngress(data)).execute()
    }


    fun createIngress(namespace: String, data: Ingress) {
        if (ingressExists(data.name, namespace)) {
            networkingV1Api.deleteNamespacedIngress(data.name, namespace).execute()
        }

        networkingV1Api.createNamespacedIngress(namespace, buildIngressDomain(namespace, data)).execute()
    }

    fun createDeployment(data: DeploymentData) {
        if (deploymentExists(data.name, data.namespace)) {
            appsV1Api.deleteNamespacedDeployment(data.name, data.namespace).execute()
        }

        appsV1Api.createNamespacedDeployment(data.namespace, buildDeployment(data)).execute()
    }


    fun createConfigMap(namespace: String, data: Environment) {
        if (configMapExists(data.name, namespace)) {
            coreV1Api.deleteNamespacedConfigMap(data.name, namespace).execute()
        }

        coreV1Api.createNamespacedConfigMap(namespace, buildConfigMapDomain(namespace, data)).execute()
    }

    fun createDeployment(namespace: String, data: Deployment) {
        if (deploymentExists(data.name, namespace)) {
            appsV1Api.deleteNamespacedDeployment(data.name, namespace).execute()
        }

        appsV1Api.createNamespacedDeployment(namespace, buildDeploymentDomain(namespace, data, mapOf())).execute()
    }

    fun createConfigMap(data: ConfigMapData) {
        if (configMapExists(data.name, data.namespace)) {
            coreV1Api.deleteNamespacedConfigMap(data.name, data.namespace)
        }

        coreV1Api.createNamespacedConfigMap(data.namespace, buildConfigMap(data)).execute()
    }

    fun createPod(data: PodData) {
        if (podExists(data.name, data.namespace)) {
            coreV1Api.deleteNamespacedPod(data.name, data.namespace).execute()
        }

        coreV1Api.createNamespacedPod(data.namespace, buildPod(data)).execute()
    }

    private fun namespaceExists(namespace: String) =
        resourceExists(coreV1Api.readNamespace(namespace)::execute)


    private fun podExists(name: String, namespace: String) =
        resourceExists(coreV1Api.readNamespacedPod(name, namespace)::execute)

    private fun serviceExists(name: String, namespace: String) =
        resourceExists(coreV1Api.readNamespacedService(name, namespace)::execute)


    private fun configMapExists(name: String, namespace: String) =
        resourceExists(coreV1Api.readNamespacedConfigMap(name, namespace)::execute)

    private fun deploymentExists(name: String, namespace: String) =
        resourceExists(appsV1Api.readNamespacedDeployment(name, namespace)::execute)

    private fun ingressExists(name: String, namespace: String) =
        resourceExists(networkingV1Api.readNamespacedIngress(name, namespace)::execute)

    private fun resourceExists(readResource: () -> Any?) = try {
        readResource() != null
    } catch (e: ApiException) {
        e.code != NOT_FOUND.value() && throw e
    }

    fun editDeploymentConfig(
        dto: ApplicationController.EditDeploymentDto,
        pod: String,
        environment: String,
        deployment: String,
        namespace: String
    ) {

        if (configMapExists(environment, namespace)) {

            coreV1Api.replaceNamespacedConfigMap(
                environment, namespace, buildConfigMapDomain(
                    namespace, Environment(
                        id = 0,
                        name = environment,
                        content = dto.properties
                    )
                )
            )
        } else {
            coreV1Api.createNamespacedConfigMap(
                namespace, buildConfigMapDomain(
                    namespace, Environment(
                        id = 0,
                        name = environment,
                        content = dto.properties
                    )
                )
            )
        }

        val existingDeployment = appsV1Api.readNamespacedDeployment(deployment, namespace).execute()
        existingDeployment.spec.template
            .also {
                it.metadata.putAnnotationsItem("kubectl.kubernetes.io/restartedAt", LocalDateTime.now().toString())
            }.spec.containers.first()
            .env(
                dto.properties.map { e ->
                    V1EnvVar()
                        .name(e.key)
                        .valueFrom(
                            V1EnvVarSource()
                                .configMapKeyRef(
                                    V1ConfigMapKeySelector()
                                        .name(environment)
                                        .key(e.key)
                                )
                        )
                }.toMutableList()
            )

        val deploymentJson = JSON.serialize(existingDeployment)

        PatchUtils.patch(
            V1Deployment::class.java,
            {
                appsV1Api.patchNamespacedDeployment(
                    deployment,
                    namespace,
                    V1Patch(deploymentJson)
                ).fieldManager("kubectl-rollout").buildCall(null)
            },
            V1Patch.PATCH_FORMAT_STRATEGIC_MERGE_PATCH,
            client
        )
    }
}