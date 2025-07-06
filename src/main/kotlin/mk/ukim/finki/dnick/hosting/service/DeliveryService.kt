package mk.ukim.finki.dnick.hosting.service

import mk.ukim.finki.dnick.hosting.model.domain.DeploymentState.*
import mk.ukim.finki.dnick.hosting.model.domain.PartialDeployment
import org.springframework.stereotype.Service

data class ResourceChanges(
    val namespace: String,
    val names: ResourceNames,
    val update: ResourceNames,
    val state: PartialDeployment,
    val changes: Set<Change>
)

enum class Change {
    SERVICE_NAME,

    ENVIRONMENT_VALUES,

    INGRESS,
    INGRESS_NAME,
    INGRESS_DELETE,

    DEPLOYMENT_NAME,
    DEPLOYMENT_STATE,
    DEPLOYMENT_IMAGE;
}


data class ResourceNames(
    val deployment: String,
    val pod: String,
    val config: String,
    val service: String,
    val ingress: String?,
)

@Service
class DeliveryService(
    private val applicationDeploymentService: ApplicationDeploymentService,
) {

    fun deploy(data: PartialDeployment) {
        try {
            deployInternal(data)
        } catch (e: Exception) {
            deleteDeployment(data)
        }
    }

    fun updateDeployment(data: ResourceChanges) {
        updateIngress(data)
        updateService(data)
        updateConfig(data)
        updateDeploymentData(data)
    }

    private fun updateDeploymentData(data: ResourceChanges) {
        if (data.changes.contains(Change.DEPLOYMENT_NAME)) {
            applicationDeploymentService.deleteDeployment(data.namespace, data.names.deployment)
            applicationDeploymentService.createDeployment(data.namespace, data.state.deployment)
            return
        }

        if (data.changes.contains(Change.ENVIRONMENT_VALUES) || data.changes.contains(Change.DEPLOYMENT_IMAGE)) {
            applicationDeploymentService.deleteDeployment(data.namespace, data.state.deployment)
            applicationDeploymentService.createDeployment(data.namespace, data.state.deployment)
            return
        }

        if (data.changes.contains(Change.DEPLOYMENT_STATE)) {
            when (data.state.deployment.state) {
                STARTED, INITIAL ->
                    applicationDeploymentService.startDeployment(data.state.deployment.name, data.namespace)

                STOPPED -> applicationDeploymentService.stopDeployment(data.state.deployment.name, data.namespace)

                FAILED -> {}
            }
            return
        }
    }

    private fun updateConfig(data: ResourceChanges) {
        if (data.changes.contains(Change.DEPLOYMENT_NAME)) {
            applicationDeploymentService.deleteConfigMap(data.namespace, data.names.config)
            for (pod in data.state.deployment.pods) {
                applicationDeploymentService.createConfigMap(data.namespace, pod.environment)
            }
            return
        }

        if (data.changes.contains(Change.ENVIRONMENT_VALUES)) {
            for (pod in data.state.deployment.pods) {
                applicationDeploymentService.deleteConfigMap(data.namespace, pod.environment)
                applicationDeploymentService.createConfigMap(data.namespace, pod.environment)
            }
            return
        }
    }

    private fun updateIngress(data: ResourceChanges) {
        val changes = data.changes

        if (changes.contains(Change.INGRESS_NAME) || changes.contains(Change.INGRESS)) {
            applicationDeploymentService.deleteIngress(data.namespace, data.names.ingress!!)
            data.state.ingress?.let { applicationDeploymentService.createIngress(data.namespace, it) }
            return
        }

        if (changes.contains(Change.INGRESS_DELETE)) {
            applicationDeploymentService.deleteIngress(data.namespace, data.names.ingress!!)
            return
        }
    }

    private fun updateService(data: ResourceChanges) {
        val changes = data.changes

        if (changes.contains(Change.SERVICE_NAME)) {
            applicationDeploymentService.deleteService(data.namespace, data.names.service)
            applicationDeploymentService.createService(data.namespace, data.state.service)
            return
        }
    }

    fun deleteDeployment(data: PartialDeployment) {
        data.ingress?.let { applicationDeploymentService.deleteIngress(data.namespace, it) }
        applicationDeploymentService.deleteService(data.namespace, data.service)
        applicationDeploymentService.deleteDeployment(data.namespace, data.deployment)
        data.deployment.pods.forEach {
            applicationDeploymentService.deleteConfigMap(data.namespace, it.environment)
        }
    }

    private fun deployInternal(data: PartialDeployment) {
        applicationDeploymentService.createNamespace(data.namespace)
        data.deployment.pods.forEach { applicationDeploymentService.createConfigMap(data.namespace, it.environment) }
        applicationDeploymentService.createDeployment(data.namespace, data.deployment)
        applicationDeploymentService.createService(data.namespace, data.service)
        data.ingress?.let { applicationDeploymentService.createIngress(data.namespace, it) }
    }

}