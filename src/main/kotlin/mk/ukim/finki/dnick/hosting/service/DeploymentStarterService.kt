package mk.ukim.finki.dnick.hosting.service

import mk.ukim.finki.dnick.hosting.deployment.DeploymentCache
import mk.ukim.finki.dnick.hosting.socket.DeploymentDoneEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class DeploymentStarterService(
    private val deploymentCache: DeploymentCache,
    private val deliveryService: DeliveryService
) {

    companion object {
        private const val CONCURRENT_IMAGE_JOBS = 20
    }

    @Async
    @EventListener(value = [DeploymentDoneEvent::class])
    fun start() {
        if (deploymentCache.jobCount() < CONCURRENT_IMAGE_JOBS) {
            deploymentCache.dequeue()?.let { deliveryService.deploy(it.data) }
        }
    }
}