package mk.ukim.finki.dnick.hosting.service

import org.springframework.stereotype.Component
import java.util.*

@Component
class ImageBuildCache {

    private val imageBuilds = mutableMapOf<UUID, ImageBuild>()

    private val queuedImageBuilds = mutableMapOf<UUID, QueuedImageBuild>()
    private val imageBuildQueue = LinkedList<UUID>()

    fun queue(imageBuild: QueuedImageBuild) {
        imageBuildQueue.add(imageBuild.uid)
        queuedImageBuilds[imageBuild.uid] = imageBuild
    }

    fun dequeue(): QueuedImageBuild? {
        return imageBuildQueue.removeFirstOrNull()?.let { queuedImageBuilds.remove(it) }?.let {
            imageBuilds[it.uid] = ImageBuild(
                uid = it.uid,
                status = it.status,
                logListener = it.logListener,
                log = it.log
            )
            it
        }
    }

    fun removeBuild(uid: UUID) {
        imageBuilds.remove(uid)
    }

    fun getImageBuild(uid: UUID) = imageBuilds[uid]

    fun jobCount() = imageBuildQueue.size

}