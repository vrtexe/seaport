package mk.ukim.finki.dnick.hosting.deployment.log

import org.springframework.stereotype.Component
import java.util.*


@Component
class DeploymentLogCache {

    private val deployments = mutableMapOf<UUID, Deployment>()

    fun queue(deployment: Deployment) {
        deployments[deployment.uid] = deployment
    }

    fun dequeue(uuid: UUID) {
        deployments.remove(uuid)
    }

    fun setWatcher(uid: UUID, watcher: AutoCloseable) {
        deployments[uid]?.watcher = watcher
    }

    fun getDeployment(uid: UUID) = deployments[uid]

}