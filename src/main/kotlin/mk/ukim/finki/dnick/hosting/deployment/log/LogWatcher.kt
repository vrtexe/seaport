package mk.ukim.finki.dnick.hosting.deployment.log

import com.google.gson.reflect.TypeToken.getParameterized
import io.kubernetes.client.PodLogs
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Pod
import io.kubernetes.client.util.Watch
import io.kubernetes.client.util.Watch.Response
import mk.ukim.finki.dnick.hosting.builder.APP_LABEL
import mk.ukim.finki.dnick.hosting.image.isRunning
import mk.ukim.finki.dnick.hosting.socket.DeploymentLogWatcherStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

@Component
class LogWatcher(
    private val client: ApiClient,
    private val coreV1Api: CoreV1Api,
    private val deploymentLogger: DeploymentLogger,
    private val deploymentCache: DeploymentLogCache,
) {

    @Async
    @EventListener
    fun startWatcher(event: DeploymentLogWatcherStartedEvent) {
        val pod = coreV1Api.listNamespacedPod(event.namespace)
            .labelSelector(selectorOf(APP_LABEL to event.deploymentName))
            .execute()

        pod.items.firstOrNull()?.metadata?.name?.let {
            coreV1Api.readNamespacedPodLog(it, event.namespace).execute().let { logs ->
                deploymentLogger.writeLog(logs, event.deploymentUid)
            }
        }

        Watch.createWatch<V1Pod>(
            client,
            coreV1Api.listNamespacedPod(event.namespace)
                .watch(true)
                .labelSelector(selectorOf(APP_LABEL to event.deploymentName))
                .buildCall(null),
            getParameterized(Response::class.java, V1Pod::class.java).type
        ).use { watchLogs(it, event) }
    }

    private fun streamLogs(pod: V1Pod, uid: UUID) {
        val logs = PodLogs()
        val inputStream = logs.streamNamespacedPodLog(pod)

        BufferedReader(InputStreamReader(inputStream)).lines()
            .forEach { line -> deploymentLogger.writeLog(line, uid) }
    }

    private fun watchLogs(watcher: Watch<V1Pod>, event: DeploymentLogWatcherStartedEvent) {
        deploymentCache.setWatcher(event.deploymentUid, watcher)
        try {
            for (response in watcher) {
                val pod = response.`object`
                if (pod.isRunning()) {
                    streamLogs(pod, event.deploymentUid)
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun selectorOf(data: Pair<String, String>): String {
        return data.toSelector()
    }

    private fun Pair<String, String>.toSelector(): String {
        return "${this.first}=${this.second}"
    }
}