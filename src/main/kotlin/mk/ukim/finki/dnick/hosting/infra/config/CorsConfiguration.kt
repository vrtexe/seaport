package mk.ukim.finki.dnick.hosting.infra.config

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

object CorsConfiguration {

    private val headers: List<String> = java.util.List.of(
        HttpHeaders.ORIGIN,
        HttpHeaders.CONTENT_TYPE,
        HttpHeaders.ACCEPT,
        HttpHeaders.RANGE,
        HttpHeaders.AUTHORIZATION,
        HttpHeaders.ETAG,
        HttpHeaders.IF_MATCH,
        HttpHeaders.IF_NONE_MATCH,
        HttpHeaders.IF_MODIFIED_SINCE,
        HttpHeaders.CACHE_CONTROL
    )

    private val methods: List<String> = java.util.List.of(
        HttpMethod.GET.toString(),
        HttpMethod.HEAD.toString(),
        HttpMethod.POST.toString(),
        HttpMethod.PUT.toString(),
        HttpMethod.DELETE.toString(),
        HttpMethod.TRACE.toString(),
        HttpMethod.OPTIONS.toString(),
        HttpMethod.PATCH.toString()
    )

    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()

        source.registerCorsConfiguration("/**", createCorsConfiguration())

        return source
    }

    private fun createCorsConfiguration(): CorsConfiguration {
        val configuration = CorsConfiguration()

        configuration.addAllowedOriginPattern("*")
        configuration.allowedHeaders = headers
        configuration.exposedHeaders = headers
        configuration.allowedMethods = methods

        return configuration
    }

}