package mk.ukim.finki.dnick.hosting.service

import io.github.oshai.kotlinlogging.KotlinLogging
import mk.ukim.finki.dnick.hosting.image.ImageJobProperties
import mk.ukim.finki.dnick.hosting.model.entity.ImageStatus
import mk.ukim.finki.dnick.hosting.model.entity.ImageTag
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.util.UUID

data class ImageBuild(
    val uid: UUID,
    val log: StringBuilder = StringBuilder(),
    var status: ImageStatus,
    val logListener: MutableMap<String, WebSocketSession> = mutableMapOf(),
    val statusListener: MutableMap<String, WebSocketSession> = mutableMapOf()
)

data class QueuedImageBuild(
    val uid: UUID,
    val log: StringBuilder = StringBuilder(),
    val status: ImageStatus,
    val imageJob: ImageJobProperties,
    val logListener: MutableMap<String, WebSocketSession> = mutableMapOf(),
    val statusListener: MutableMap<String, WebSocketSession> = mutableMapOf()
)

@Component
class ImageBuildQueue(
    private val imageBuildCache: ImageBuildCache,
    private val buildStarterService: BuildStarterService
) {

    fun queueImageBuild(image: ImageTag, imageJob: ImageJobProperties) {
        imageBuildCache.queue(
            QueuedImageBuild(
                uid = image.hash,
                status = image.status,
                imageJob = imageJob
            )
        )

        buildStarterService.startBuild()
    }
}

