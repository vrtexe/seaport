package mk.ukim.finki.dnick.hosting.controller

import mk.ukim.finki.dnick.hosting.service.ApplicationPersistenceService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("$API_V1_PATH/application")
class ApplicationController(val applicationPersistenceService: ApplicationPersistenceService) {


    @PostMapping
    fun createApplication(request: ApplicationCreateRequest) {

    }

}