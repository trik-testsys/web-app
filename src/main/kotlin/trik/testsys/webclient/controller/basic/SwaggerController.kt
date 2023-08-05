package trik.testsys.webclient.controller.basic

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class SwaggerController {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @ApiOperation(value = "Get Swagger documentation")
    @ApiResponse(code = 302, message = "Redirect to /swagger-ui/index.html")
    @GetMapping("/swagger", produces = [MediaType.TEXT_HTML_VALUE])
    fun redirect(): ResponseEntity<Void> {
        logger.info("Client requested Swagger documentation.")
        val headers = HttpHeaders()
        headers.location = URI.create("/swagger-ui/index.html")
        return ResponseEntity(headers, HttpStatus.FOUND)
    }
}