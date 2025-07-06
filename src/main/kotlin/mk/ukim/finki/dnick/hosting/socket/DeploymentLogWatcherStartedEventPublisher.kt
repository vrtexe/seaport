package mk.ukim.finki.dnick.hosting.socket

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component


@Component
class DeploymentLogWatcherStartedEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    fun publish(event: DeploymentLogWatcherStartedEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}