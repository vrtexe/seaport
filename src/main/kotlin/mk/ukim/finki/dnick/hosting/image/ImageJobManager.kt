package mk.ukim.finki.dnick.hosting.image

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kubernetes.client.openapi.apis.BatchV1Api
import io.kubernetes.client.openapi.models.V1Job
import mk.ukim.finki.dnick.hosting.model.entity.ImageStatus
import mk.ukim.finki.dnick.hosting.service.ImageBuildStatusHandler
import mk.ukim.finki.dnick.hosting.socket.BuildStartedEvent
import mk.ukim.finki.dnick.hosting.socket.BuildStartedEventPublisher
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class ImageJobManager(
    private val batchV1Api: BatchV1Api,
    private val imageJobBuilder: ImageJobBuilder,
    private val imageBuildStatusHandler: ImageBuildStatusHandler,
    private val buildStartedEventPublisher: BuildStartedEventPublisher,
) {

    fun buildImage(properties: ImageJobProperties) {
        log.info { "Building image: ${properties.name}:${properties.version}" }
        try {
            imageJobBuilder.newImageBuilder(properties).let {
                imageBuildStatusHandler.updateStatus(ImageStatus.started, properties.imageUid)
                startJob(it)
                buildStartedEventPublisher.publish(
                    BuildStartedEvent(
                        imageKey = properties.key,
                        jobName = it.metadata.name,
                        namespace = it.metadata.namespace,
                        imageUid = properties.imageUid
                    )
                )
                it
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to build image: ${properties.name}:${properties.version}" }
            imageBuildStatusHandler.updateStatus(ImageStatus.failed, properties.imageUid)
        }

    }

    fun startJob(job: V1Job) {
        log.info { "Creating image builder job" }
        batchV1Api.createNamespacedJob(job.metadata.namespace, job).execute()
        log.info { "Created image builder job" }
    }

}