package mk.ukim.finki.dnick.hosting.socket

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class BuildStartedEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    fun publish(event: BuildStartedEvent) {
        applicationEventPublisher.publishEvent(event)
    }

}