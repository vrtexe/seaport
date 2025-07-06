package mk.ukim.finki.dnick.hosting.infra.config

import mk.ukim.finki.dnick.hosting.infra.config.CorsConfiguration.corsConfigurationSource
import mk.ukim.finki.dnick.hosting.infra.config.auth.AuthenticationJwtConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(private val authenticationConverter: AuthenticationJwtConverter) {

    @Bean
    fun cmsSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().permitAll()
            }
            .oauth2ResourceServer {
                it.jwt { jwt -> jwt.jwtAuthenticationConverter(authenticationConverter) }
            }
            .build()
    }
}