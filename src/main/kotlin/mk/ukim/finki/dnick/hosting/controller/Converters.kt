package mk.ukim.finki.dnick.hosting.controller

import com.fasterxml.jackson.databind.ObjectMapper
import mk.ukim.finki.dnick.hosting.image.ImagesExeRequest
import mk.ukim.finki.dnick.hosting.service.ApplicationRequest
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class ImagesExeRequestConverter(private val objectMapper: ObjectMapper) : Converter<String, ImagesExeRequest> {

    override fun convert(source: String): ImagesExeRequest? {
        return objectMapper.readValue(source, ImagesExeRequest::class.java)
    }
}

@Component
class BodyConverter(private val objectMapper: ObjectMapper) : Converter<String, ApplicationRequest> {

    override fun convert(source: String): ApplicationRequest? {
        return objectMapper.readValue(source, ApplicationRequest::class.java)
    }

}