package mk.ukim.finki.dnick.hosting.socket

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class StatusListenerAttachedEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    fun publish(event: StatusListenerAttachedEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}