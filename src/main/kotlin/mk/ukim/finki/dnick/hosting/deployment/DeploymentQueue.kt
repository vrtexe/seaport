package mk.ukim.finki.dnick.hosting.deployment

import mk.ukim.finki.dnick.hosting.service.DeploymentStarterService
import org.springframework.stereotype.Component


@Component
class DeploymentQueue(
    private val deploymentStarterService: DeploymentStarterService,
    private val deploymentCache: DeploymentCache
) {

    fun queueDeployment(deployment: Deployment) {
        deploymentCache.queue(deployment)
        deploymentStarterService.start()
    }
}

