package mk.ukim.finki.dnick.hosting

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class HostingApplication

fun main(args: Array<String>) {
    runApplication<HostingApplication>(*args)
}
