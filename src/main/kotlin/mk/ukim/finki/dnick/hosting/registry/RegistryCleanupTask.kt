package mk.ukim.finki.dnick.hosting.registry

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kubernetes.client.Exec
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.util.Streams
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.OutputStreamWriter

private val log = KotlinLogging.logger {}

@Component
class RegistryCleanupTask(private val apiClient: ApiClient, private val coreV1Api: CoreV1Api) {

    companion object {
        private const val REGISTRY_NAMESPACE = "registry-internal"
        private const val REGISTRY_SELECTOR = "app=registry-app"
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun run() {
        log.info { "[Registry-cleanup]: Initialize garbage collection of registry" }

        coreV1Api.listNamespacedPod(REGISTRY_NAMESPACE)
            .labelSelector(REGISTRY_SELECTOR)
            .execute()
            .items
            .forEach {
                val cmd = Exec(apiClient)
                val process = cmd.exec(
                    it,
                    arrayOf("registry", "garbage-collect", "/etc/docker/registry/config.yml"),
                    false
                )

                Streams.copy(process.inputStream, System.out)

                process.waitFor()
                process.destroy()

                log.info { "[Registry-cleanup] ExitValue: ${process.exitValue()}" }
            }
    }
}