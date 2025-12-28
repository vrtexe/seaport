package mk.ukim.finki.dnick.hosting.registry

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import mk.ukim.finki.dnick.hosting.registry.RegistryMediaType.Companion.APPLICATION_DOCKER_MANIFEST_V2_JSON_VALUE

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.stereotype.Component
import org.springframework.util.MimeType
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Component
class RegistryClientFactory(private val properties: RegistryProperties) {

    fun createWebClient(): WebClient {
        val httpClient = HttpClient.create()
            .proxyWithSystemProperties()

        return WebClient.builder()
            .baseUrl(properties.url)
            .defaultHeader(ACCEPT, APPLICATION_DOCKER_MANIFEST_V2_JSON_VALUE)
            .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs({ conf: ClientCodecConfigurer -> this.configureCodecs(conf) })
            .build()
    }

    private fun configureCodecs(conf: ClientCodecConfigurer) {
        val objectMapper = buildJsonMapper()

        conf.defaultCodecs().jackson2JsonEncoder(
            Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON, *RegistryMediaType.mimeTypes())
        )

        conf.defaultCodecs().jackson2JsonDecoder(
            Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON, *RegistryMediaType.mimeTypes())
        )
    }

    private fun buildJsonMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.findAndRegisterModules()
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        return objectMapper
    }
}