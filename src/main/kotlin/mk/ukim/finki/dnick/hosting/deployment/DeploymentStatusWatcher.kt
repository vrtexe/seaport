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
import mk.ukim.finki.dnick.hosting.image.isReady
import mk.ukim.finki.dnick.hosting.model.domain.DeploymentState
import mk.ukim.finki.dnick.hosting.socket.DeploymentStartedEvent
import mk.ukim.finki.dnick.hosting.socket.StatusListenerAttachedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.*

private val log = KotlinLogging.logger {}

@Component
class DeploymentStatusWatcher(
    private val coreV1Api: CoreV1Api,
    private val client: ApiClient,
    private val deploymentStatusHandler: DeploymentStatusHandler,
) {

    @EventListener
    fun onDeployStarted(event: DeploymentStartedEvent): Boolean {
        return watchStatus(event.toStatusWatcherData())
    }

    @EventListener
    fun onListenerAttach(event: StatusListenerAttachedEvent): Boolean {
        return watchStatus(event.toStatusWatcherData())
    }

    fun watchStatus(event: StatusWatcherData): Boolean {
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

    private fun isPodCompleted(watcher: Watch<V1Pod>, event: StatusWatcherData): Boolean {
        for (response in watcher) {
            val pod = response.`object`

            val status = getPodStatus(pod)
            if (status != null) {
                deploymentStatusHandler.updateStatus(status, event.deploymentUid)
                return true
            }
        }

        return false
    }

    private fun getPodStatus(podStatus: V1Pod): DeploymentState? {
        return podStatus.getTerminatedStatus()?.let {
            when (it) {
                CompletedReason -> DeploymentState.STARTED
                ErrorReason -> DeploymentState.FAILED
                else -> null
            }
        } ?: podStatus.isReady().let { if (it) DeploymentState.STARTED else null }
    }

    private fun selectorOf(data: Pair<String, String>): String {
        return data.toSelector()
    }

    private fun Pair<String, String>.toSelector(): String {
        return "${this.first}=${this.second}"
    }


    fun StatusListenerAttachedEvent.toStatusWatcherData() = StatusWatcherData(
        namespace = this.namespace,
        deploymentUid = this.deploymentUid,
        deploymentName = this.deploymentName
    )

    fun DeploymentStartedEvent.toStatusWatcherData() = StatusWatcherData(
        namespace = this.namespace,
        deploymentUid = this.deploymentUid,
        deploymentName = this.deploymentName
    )

    data class StatusWatcherData(
        val namespace: String,
        val deploymentUid: UUID,
        val deploymentName: String,
    )
}
