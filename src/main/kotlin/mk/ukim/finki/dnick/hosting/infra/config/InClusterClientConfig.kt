package mk.ukim.finki.dnick.hosting.infra.config

import io.kubernetes.client.util.ClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("cluster")
class InClusterClientConfig {

    @Bean
    fun kubernetesClient() = ClientBuilder.cluster().build()
        .also { io.kubernetes.client.openapi.Configuration.setDefaultApiClient(it) }!!

}