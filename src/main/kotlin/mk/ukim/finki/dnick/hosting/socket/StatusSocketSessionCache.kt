package mk.ukim.finki.dnick.hosting.socket

import io.kubernetes.client.informer.ResourceEventHandler
import io.kubernetes.client.informer.SharedIndexInformer
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.openapi.ApiCallback
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.models.V1Deployment
import io.kubernetes.client.openapi.models.V1DeploymentList
import mk.ukim.finki.dnick.hosting.repository.DeploymentRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
class StatusSocketSessionCache(
    private val appsV1Api: AppsV1Api,
    private val client: ApiClient,
    private val deploymentRepository: DeploymentRepository,
    private val informerFactory: SharedInformerFactory
) {

    private val socketSessions: MutableMap<String, WebSocketSession> = ConcurrentHashMap()
    private val socketSubscription: MutableMap<String, Set<Int>> = ConcurrentHashMap()

    private val deploymentWatch: MutableMap<Int, SharedIndexInformer<V1Deployment>> = ConcurrentHashMap()

    @Transactional(readOnly = true)
    fun subscribe(session: WebSocketSession, deployments: Set<Int>) {
        socketSubscription[session.id] = socketSubscription[session.id]?.let { it + deployments } ?: deployments
        deployments.mapNotNull {
            deploymentRepository.findByIdOrNull(it)?.let {
                DeployWatchDto(
                    id = it.id!!,
                    name = it.name,
                    namespace = it.application.namespace.name
                )
            }
        }.also {
            notifyClientAboutInitialStatuses(session, it)
        }.forEach {
            createWatch(session, it)
        }
    }

    fun unsubscribe(session: WebSocketSession, deployments: Set<Int>) {
        socketSubscription[session.id] = socketSubscription[session.id]?.let { it - deployments } ?: setOf()
        for (deploymentId in deployments) {
            deploymentWatch[deploymentId]?.stop()
            deploymentWatch.remove(deploymentId)
        }

    }

    fun add(session: WebSocketSession) {
        this.socketSessions[session.id] = session
    }

    fun remove(session: WebSocketSession) {
        this.socketSessions.remove(session.id)
        unsubscribe(session, this.socketSubscription[session.id] ?: setOf())
        this.socketSubscription.remove(session.id)
    }

    fun get(sessionId: String): WebSocketSession? {
        return socketSessions[sessionId]
    }

    data class DeployWatchDto(
        val id: Int,
        val name: String,
        val namespace: String
    )

    fun createWatch(session: WebSocketSession, deployment: DeployWatchDto) {
        val deploymentInformer = informerFactory.sharedIndexInformerFor({
            appsV1Api.listNamespacedDeployment(deployment.namespace)
                .watch(it.watch)
                .buildCall(null)
        }, V1Deployment::class.java, V1DeploymentList::class.java)

        deploymentInformer.addEventHandler(object : ResourceEventHandler<V1Deployment> {
            override fun onAdd(obj: V1Deployment?) {
                if (deployment.name == obj?.metadata?.name) {
                    sendMessage(session, deployment.id, obj)
                }
            }

            override fun onUpdate(oldObj: V1Deployment?, newObj: V1Deployment?) {
                if (deployment.name == newObj?.metadata?.name) {
                    sendMessage(session, deployment.id, newObj)
                }
            }

            override fun onDelete(obj: V1Deployment?, deletedFinalStateUnknown: Boolean) {
                if (deployment.name == obj?.metadata?.name) {
                    sendMessage(session, deployment.id, obj)
                }
            }
        })

        informerFactory.startAllRegisteredInformers()
    }

    fun sendMessage(socket: WebSocketSession, deploymentId: Int, deployment: V1Deployment) {
        socket.sendMessage(
            TextMessage(
                """
                    {
                        "id": ${deploymentId},
                        "status": "${getStatus(deployment)}"
                    }
                    """.trimIndent()
            )
        )
    }

    fun getStatus(obj: V1Deployment?): String {
        return obj?.let {
            if (obj.status.readyReplicas != null && obj.status.readyReplicas > 0) {
                "READY"
            } else if (obj.status.availableReplicas != null && obj.status.availableReplicas > 0) {
                "UNKNOWN"
            } else if (obj.status.unavailableReplicas != null && obj.status.unavailableReplicas > 0) {
                "UNAVAILABLE"
            } else {
                "UNKNOWN"
            }
        } ?: "UNKNOWN"
    }

    fun notifyClientAboutInitialStatuses(session: WebSocketSession, deployment: List<DeployWatchDto>) {
        deployment.groupBy { it.namespace }.forEach { namespace, deployments ->
            notifyClient(session, namespace, deployments)
        }
    }

    fun notifyClient(session: WebSocketSession, namespace: String, clientDeployment: List<DeployWatchDto>) {
        val deploymentNamesData = clientDeployment.map { d -> d.name to d }.toMap()
        appsV1Api.listNamespacedDeployment(namespace)
            .executeAsync(object : ApiCallback<V1DeploymentList> {
                override fun onSuccess(
                    deployments: V1DeploymentList?,
                    p1: Int,
                    p2: MutableMap<String, MutableList<String>>?
                ) {
                    deployments?.let {
                        it.items.forEach { d ->
                            deploymentNamesData.get(d.metadata.name)?.let { cd ->
                                sendMessage(session, cd.id, d)
                            }
                        }
                    }
                }

                override fun onFailure(p0: ApiException?, p1: Int, p2: MutableMap<String, MutableList<String>>?) {
                }

                override fun onUploadProgress(p0: Long, p1: Long, p2: Boolean) {
                }

                override fun onDownloadProgress(p0: Long, p1: Long, p2: Boolean) {
                }
            })
    }
}