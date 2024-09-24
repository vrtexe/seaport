package mk.ukim.finki.dnick.hosting.infra.config

import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.BatchV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.apis.NetworkingV1Api
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class KubernetesConfig {

    @Bean
    fun coreV1Api(client: ApiClient) = CoreV1Api(client)

    @Bean
    fun appsV1Api(client: ApiClient) = AppsV1Api(client)

    @Bean
    fun batchV1Api(client: ApiClient) = BatchV1Api(client)

    @Bean
    fun dynamicKubernetesApi(client: ApiClient) = DynamicKubernetesApi("", "v1", "", client)

    @Bean
    fun networkingV1Api(client: ApiClient) = NetworkingV1Api(client)

    @Bean
    fun informerFactory(client: ApiClient) = SharedInformerFactory(client)

}