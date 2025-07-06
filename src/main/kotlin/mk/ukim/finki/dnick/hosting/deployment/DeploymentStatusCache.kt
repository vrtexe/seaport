package mk.ukim.finki.dnick.hosting.deployment

import org.springframework.stereotype.Component
import java.util.*

@Component
class DeploymentStatusCache(
    private val deploymentCache: DeploymentCache,
) {

    private val deployments = mutableMapOf<UUID, Deployment>()

    fun queue(deployment: Deployment) {
        deploymentCache.findDeployment(deployment.uid)?.let { deployments[it.uid] = it }
        if (deployments[deployment.uid] == null) {
            deployments[deployment.uid] = deployment
        }
    }

    fun getDeployment(uid: UUID) = deployments[uid]

    fun remove(uid: UUID) {
        deployments.remove(uid)
    }

}