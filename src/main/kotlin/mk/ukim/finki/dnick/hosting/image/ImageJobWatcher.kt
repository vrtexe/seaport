package mk.ukim.finki.dnick.hosting.image

import com.google.gson.reflect.TypeToken.getParameterized
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kubernetes.client.PodLogs
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.BatchV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Pod
import io.kubernetes.client.util.Watch
import io.kubernetes.client.util.Watch.Response
import mk.ukim.finki.dnick.hosting.model.entity.ImageStatus
import mk.ukim.finki.dnick.hosting.service.BuildStarterService
import mk.ukim.finki.dnick.hosting.service.ImageBuildLogger
import mk.ukim.finki.dnick.hosting.service.ImageBuildStatusHandler
import mk.ukim.finki.dnick.hosting.socket.BuildStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

private val log = KotlinLogging.logger {}

@Component
class ImageJobWatcher(
    private val batchV1Api: BatchV1Api,
    private var coreV1Api: CoreV1Api,
    private var client: ApiClient,
    private val imageBuildStatusHandler: ImageBuildStatusHandler,
    private val buildStarterService: BuildStarterService,
    private val imageBuildLogger: ImageBuildLogger,
    private val properties: ImageBuilderProperties,
) {

    @EventListener
    fun waitForJob(event: BuildStartedEvent): Boolean {
        log.info { "Waiting for image build: ${event.imageKey}" }
        return Watch.createWatch<V1Pod>(
            client,
            coreV1Api.listNamespacedPod(event.namespace)
                .watch(true)
                .labelSelector(selectorOf(event.imageKey))
                .buildCall(null),
            getParameterized(Response::class.java, V1Pod::class.java).type
        ).use {
            isPodCompleted(it, event)
        }
    }


    fun streamLogs(pod: V1Pod, imageUid: UUID) {
        val logs = PodLogs()

        val inputStream = logs.streamNamespacedPodLog(pod)

        BufferedReader(InputStreamReader(inputStream)).lines()
            .forEach { line ->
                imageBuildLogger.writeLog(line, imageUid)
                log.info { line }
            }
    }


    private fun isPodCompleted(watcher: Watch<V1Pod>, event: BuildStartedEvent): Boolean {
        for (response in watcher) {
            val pod = response.`object`

            if (pod.isRunning()) {
                streamLogs(pod, event.imageUid)
            }

            val podStatus = pod.getTerminatedStatus()
            if (podStatus != null) {
                getImageStatus(podStatus)?.let {
                    imageBuildStatusHandler.updateStatus(it, event.imageUid)
                    buildStarterService.startBuild()
                    if (it == ImageStatus.failed) {
                        batchV1Api.deleteNamespacedJob(event.jobName, event.namespace).executeAsync(null)
                    }
                }

                if (podStatus == CompletedReason || podStatus == ErrorReason) {
                    log.info { "Image build finished with status: $podStatus" }
                }

                return podStatus == CompletedReason || podStatus == ErrorReason
            }
        }

        return false
    }

    private fun getImageStatus(podStatus: String): ImageStatus? {
        return when (podStatus) {
            CompletedReason -> ImageStatus.completed
            ErrorReason -> ImageStatus.failed
            else -> null
        }
    }

    private fun selectorOf(imageValue: String): String {
        return "${properties.labels.app.toSelector()},${properties.labels.image.copy(value = imageValue).toSelector()}"
    }

}