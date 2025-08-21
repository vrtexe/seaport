package mk.ukim.finki.dnick.hosting.serviceregistry.api

import org.springframework.web.bind.annotation.GetMapping

@ApiV1Controller
class ServiceStatusController(
    private val statusWatcherService: StatusWatcherService
) {

    @GetMapping("/await/database")
    fun awaitDatabaseReady(): Boolean {
        return statusWatcherService.awaitPodUntilReady(
            StatusWatchRequest(
                namespace = "database",
                selector = selectorOf(APP_LABEL to "database")
            )
        )
    }

}