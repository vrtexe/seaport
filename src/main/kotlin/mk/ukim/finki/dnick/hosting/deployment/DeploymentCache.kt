package mk.ukim.finki.dnick.hosting.deployment

import org.springframework.stereotype.Component
import java.util.*


@Component
class DeploymentCache {

    private val runningQueue = mutableMapOf<UUID, Deployment>()

    private val queued = mutableMapOf<UUID, Deployment>()
    private val queue = LinkedList<UUID>()

    fun queue(deployment: Deployment) {
        if (isPresent(deployment)) return
        queue.add(deployment.uid)
        queued[deployment.uid] = deployment
    }

    private fun isPresent(deployment: Deployment): Boolean {
        return queue.contains(deployment.uid) ||
                runningQueue.containsKey(deployment.uid) ||
                queued.containsKey(deployment.uid)
    }

    fun dequeue(): Deployment? {
        return queue.removeFirstOrNull()?.let { queued.remove(it) }?.let {
            runningQueue[it.uid] = it
            it
        }
    }

    fun remove(uid: UUID) {
        runningQueue.remove(uid)
    }

    fun getDeployment(uid: UUID) = runningQueue[uid]
    fun findDeployment(uid: UUID) = runningQueue[uid] ?: queued[uid]
    fun jobCount() = queue.size

}