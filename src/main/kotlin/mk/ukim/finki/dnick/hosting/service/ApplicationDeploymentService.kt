package mk.ukim.finki.dnick.hosting.service

import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.apis.NetworkingV1Api
import mk.ukim.finki.dnick.hosting.builder.*
import mk.ukim.finki.dnick.hosting.image.ImageData
import mk.ukim.finki.dnick.hosting.model.domain.Deployment
import mk.ukim.finki.dnick.hosting.model.domain.Environment
import mk.ukim.finki.dnick.hosting.model.domain.Ingress
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import mk.ukim.finki.dnick.hosting.model.domain.Service as DomainService

@Service
class ApplicationDeploymentService(
    private val coreV1Api: CoreV1Api,
    private val appsV1Api: AppsV1Api,
    private val networkingV1Api: NetworkingV1Api
) {

    fun createNamespace(namespace: String) {
        if (namespaceExists(namespace)) {
            return
        }

        val namespaceV1 = buildNamespace(namespace)
        coreV1Api.createNamespace(namespaceV1).execute()
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
}