package mk.ukim.finki.dnick.hosting.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.nio.charset.StandardCharsets.UTF_8

@Controller
class FileUploadController {

    @GetMapping("/uploadFile")
    fun uploader(): ResponseEntity<String> {
        return ResponseEntity.ok(ClassPathResource("test.html").getContentAsString(UTF_8))
    }

}