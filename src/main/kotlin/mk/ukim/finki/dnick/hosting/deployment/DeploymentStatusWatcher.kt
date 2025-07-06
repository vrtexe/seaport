package mk.ukim.finki.dnick.hosting.deployment

import com.google.gson.reflect.TypeToken.getParameterized
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Pod
import io.kubernetes.client.util.Watch
import io.kubernetes.client.util.Watch.Response
import mk.ukim.finki.dnick.hosting.builder.APP_LABEL
import mk.ukim.finki.dnick.hosting.image.CompletedReason
import mk.ukim.finki.dnick.hosting.image.ErrorReason
import mk.ukim.finki.dnick.hosting.image.getTerminatedStatus
import mk.ukim.finki.dnick.hosting.socket.DeploymentStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class DeploymentStatusWatcher(
    private val coreV1Api: CoreV1Api,
    private val client: ApiClient,
    private val deploymentStatusHandler: DeploymentStatusHandler,
) {

    @EventListener
    fun run(event: DeploymentStartedEvent): Boolean {
        log.info { "Waiting for deployment build: ${event.deploymentName}" }
        return Watch.createWatch<V1Pod>(
            client,
            coreV1Api.listNamespacedPod(event.namespace)
                .watch(true)
                .labelSelector(selectorOf(APP_LABEL to event.deploymentName))
                .buildCall(null),
            getParameterized(Response::class.java, V1Pod::class.java).type
        ).use {
            isPodCompleted(it, event)
        }
    }


    private fun isPodCompleted(watcher: Watch<V1Pod>, event: DeploymentStartedEvent): Boolean {
        for (response in watcher) {
            val pod = response.`object`

            val podStatus = pod.getTerminatedStatus()
            if (podStatus != null) {
                getPodStatus(podStatus)?.let {
                    deploymentStatusHandler.updateStatus(it, event.deploymentUid)
                }

                if (podStatus == CompletedReason || podStatus == ErrorReason) {
                    log.info { "Image build finished with status: $podStatus" }
                }

                return podStatus == CompletedReason || podStatus == ErrorReason
            }
        }

        return false
    }

    private fun getPodStatus(podStatus: String): mk.ukim.finki.dnick.hosting.model.domain.DeploymentState? {
        return when (podStatus) {
            CompletedReason -> mk.ukim.finki.dnick.hosting.model.domain.DeploymentState.STARTED
            ErrorReason -> mk.ukim.finki.dnick.hosting.model.domain.DeploymentState.FAILED
            else -> null
        }
    }

    private fun selectorOf(data: Pair<String, String>): String {
        return data.toSelector()
    }

    private fun Pair<String, String>.toSelector(): String {
        return "${this.first}=${this.second}"
    }
}
