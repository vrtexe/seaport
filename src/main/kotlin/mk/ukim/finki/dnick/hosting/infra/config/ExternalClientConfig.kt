package mk.ukim.finki.dnick.hosting.infra.config


import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.KubeConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import java.io.FileReader
import java.io.Reader
import java.io.StringReader

@Configuration
@Profile("external")
class ExternalClientConfig {

    @Bean
    fun kubernetesClient(properties: KubernetesProperties) =
        ClientBuilder
            .kubeconfig(KubeConfig.loadKubeConfig(createKubeConfigReader(properties)))
            .build()
            .also { io.kubernetes.client.openapi.Configuration.setDefaultApiClient(it) }!!


    fun createKubeConfigReader(properties: KubernetesProperties): Reader =
        when (properties.format) {
            KubernetesConfigFormat.CLASSPATH -> FileReader(ClassPathResource(properties.data).file)
            KubernetesConfigFormat.STRING -> StringReader(properties.data)
            KubernetesConfigFormat.FILE -> FileReader(properties.data)
        }

}