package trik.testsys.webclient.controllers.basic

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/health")
    fun getHealth(): ResponseEntity<Any> {
        logger.info("Client requested health info.")
        return ResponseEntity.ok().build()
    }
}