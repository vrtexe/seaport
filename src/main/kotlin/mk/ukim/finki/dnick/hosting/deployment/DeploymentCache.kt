package mk.ukim.finki.dnick.hosting.deployment

import org.springframework.stereotype.Component
import java.util.*


@Component
class DeploymentCache {

    private val runningQueue = mutableMapOf<UUID, Deployment>()

    private val queued = mutableMapOf<UUID, Deployment>()
    private val queue = LinkedList<UUID>()

    fun queue(deployment: Deployment) {
        queue.add(deployment.uid)
        queued[deployment.uid] = deployment
    }

    fun dequeue(): Deployment? {
        return queue.removeFirstOrNull()?.let { queued.remove(it) }?.let {
            runningQueue[it.uid] = it
            it
        }
    }

    fun removeBuild(uid: UUID) {
        runningQueue.remove(uid)
    }

    fun getDeployment(uid: UUID) = runningQueue[uid]

    fun jobCount() = queue.size

}