package mk.ukim.finki.dnick.hosting.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import mk.ukim.finki.dnick.hosting.model.entity.ImageLog
import mk.ukim.finki.dnick.hosting.model.entity.ImageStatus
import mk.ukim.finki.dnick.hosting.model.entity.ImageTag
import mk.ukim.finki.dnick.hosting.repository.ImageLogRepository
import mk.ukim.finki.dnick.hosting.repository.ImageTagRepository
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.UUID

private val log = KotlinLogging.logger {}

@Component
class ImageBuildStatusHandler(
    private val objectMapper: ObjectMapper,
    private val imageBuildCache: ImageBuildCache,
    private val imageTagRepository: ImageTagRepository,
    private val imageLogRepository: ImageLogRepository
) {


    fun subscribe(imageUid: UUID, socket: WebSocketSession) {
        imageBuildCache.getImageBuild(imageUid)?.let {
            it.statusListener[socket.id] = socket
            sendStatus(it, socket)
        }
    }

    fun unsubscribe(imageUid: UUID, socket: WebSocketSession) {
        imageBuildCache.getImageBuild(imageUid)?.statusListener?.remove(socket.id)
    }

    fun updateStatus(status: ImageStatus, imageUid: UUID) {
        imageBuildCache.getImageBuild(imageUid)?.let {
            it.status = status
            handleStatus(status, it)
            it.statusListener.values.forEach { socket ->
                sendStatus(it, socket)
            }
        }
    }

    private fun handleStatus(status: ImageStatus, build: ImageBuild) {
        saveStatus(status, build.uid)?.let {
            when (status) {
                ImageStatus.completed -> handleBuildComplete(it, build)
                ImageStatus.failed -> handleBuildComplete(it, build)
                ImageStatus.started -> null
                ImageStatus.initialized -> null
            }
        }
    }

    private fun handleBuildComplete(imageTag: ImageTag, build: ImageBuild) {
        saveLog(imageTag, build)
        cleanupBuild(build.uid)
    }

    private fun cleanupBuild(imageUid: UUID) {
        imageBuildCache.removeBuild(imageUid)
    }

    private fun saveLog(imageTag: ImageTag, build: ImageBuild) {
        imageTag.log?.let { log ->
            log.data = build.log.toString()
            imageLogRepository.save(log)
        } ?: imageLogRepository.save(
            ImageLog(
                data = build.log.toString(),
                image = imageTag,
            )
        )
    }

    private fun saveStatus(status: ImageStatus, imageUid: UUID): ImageTag? {
        return imageTagRepository.findByHash(imageUid)?.let {
            it.status = status
            imageTagRepository.saveAndFlush(it)
        }
    }

    private fun sendStatus(imageBuild: ImageBuild, socket: WebSocketSession) {
        toStatusJson(imageBuild)?.let { socket.sendMessage(TextMessage(it)) }
    }


    private fun toStatusJson(imageBuild: ImageBuild): String? {
        try {
            return objectMapper.writeValueAsString(imageBuild.toStatusMessageResponse())
        } catch (e: Exception) {
            log.error(e) { "Could not serialize json" }
        }
        return null;
    }

    fun ImageStatus.toMessageResponse() = ImageLogStatusMessage.valueOf(this.name.uppercase())

    fun ImageBuild.toStatusMessageResponse() = ImageStatusMessage(
        status = this.status.toMessageResponse(),
    )
}