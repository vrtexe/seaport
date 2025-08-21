package mk.ukim.finki.dnick.hosting.serviceregistry.api

import com.google.gson.reflect.TypeToken.getParameterized
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Pod
import io.kubernetes.client.util.Watch
import io.kubernetes.client.util.Watch.Response
import org.springframework.stereotype.Service

@Service
class StatusWatcherService(
    private val coreV1Api: CoreV1Api,
    private val client: ApiClient,
) {

    fun awaitPodUntilReady(request: StatusWatchRequest): Boolean {
        return Watch.createWatch<V1Pod>(
            client, coreV1Api.listNamespacedPod(request.namespace)
                .watch(true)
                .labelSelector(request.selector)
                .buildCall(null),
            getParameterized(Response::class.java, V1Pod::class.java).type
        ).use {
            isPodCompleted(it)
        }
    }

    private fun isPodCompleted(watcher: Watch<V1Pod>): Boolean {
        for (response in watcher) {
            val pod = response.`object`
            if (pod.isReady()) {
                return true
            }
        }

        return false
    }

    private fun V1Pod.isReady() =
        this.status.conditions.find { it.type == "Ready" && it.status == "True" } != null

}