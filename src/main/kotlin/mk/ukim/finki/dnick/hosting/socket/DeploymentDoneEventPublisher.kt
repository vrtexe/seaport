package mk.ukim.finki.dnick.hosting.socket

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class DeploymentDoneEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    fun publish() {
        applicationEventPublisher.publishEvent(DeploymentDoneEvent())
    }

}