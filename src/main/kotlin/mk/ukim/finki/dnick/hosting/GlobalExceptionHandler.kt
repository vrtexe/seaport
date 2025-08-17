package mk.ukim.finki.dnick.hosting

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler
    fun handleGenericException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error { e }

        return ResponseEntity.internalServerError().build()
    }
}