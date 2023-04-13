package trik.testsys.webclient.controllers.basic

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @ApiOperation(
        value = "Get info about service health.",
        notes = "Returns service health.",
    )
    @ApiResponse(code = 200, message = "OK if service is alive.")
    @GetMapping("/health")
    fun getHealth(): ResponseEntity<Void> {
        logger.info("Client requested health info.")
        return ResponseEntity.ok().build()
    }
}