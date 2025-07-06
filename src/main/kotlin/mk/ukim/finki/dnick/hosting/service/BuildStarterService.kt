package mk.ukim.finki.dnick.hosting.service

import mk.ukim.finki.dnick.hosting.image.ImageBuildCache
import mk.ukim.finki.dnick.hosting.image.ImageJobManager
import mk.ukim.finki.dnick.hosting.socket.BuildDoneEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class BuildStarterService(
    private val imageBuildCache: ImageBuildCache,
    private val imageJobManager: ImageJobManager
) {

    companion object {
        private const val CONCURRENT_IMAGE_JOBS = 5
    }

    @Async
    @EventListener(value = [BuildDoneEvent::class])
    fun startBuild() {
        if (imageBuildCache.jobCount() < CONCURRENT_IMAGE_JOBS) {
            imageBuildCache.dequeue()?.let { imageJobManager.buildImage(it.imageJob) }
        }
    }
}