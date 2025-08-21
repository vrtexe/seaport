package mk.ukim.finki.dnick.hosting.serviceregistry

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ServiceRegistryApplication

fun main(args: Array<String>) {
	runApplication<ServiceRegistryApplication>(*args)
}
