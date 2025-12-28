package mk.ukim.finki.dnick.hosting.webdav

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class WebdavResourceCleanupTask(private val webDavClient: WebDavClient) {

    @Scheduled(cron = "0 0 0 * * *")
    fun run() {
        webDavClient.fetchAll().filter {
            it.modified?.let { m -> Duration.between(m.toInstant(), Instant.now()).toDays() >= 1 } ?: true
        }.forEach { webDavClient.delete(it.path.removePrefix("/dav")) }
    }
}