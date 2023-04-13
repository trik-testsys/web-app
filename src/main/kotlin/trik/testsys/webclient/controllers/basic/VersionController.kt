package trik.testsys.webclient.controllers.basic

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

import trik.testsys.webclient.models.basic.AppVersion

@RestController
class VersionController(@Value("\${app.version}") val appVersion: String) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @ApiOperation(
        value = "Get info about service version.",
        notes = "Returns service version.",
        response = AppVersion::class,
    )
    @ApiResponse(code = 200, message = "Json with service version.")
    @GetMapping("/version", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getVersion(): ResponseEntity<AppVersion> {
        logger.info("Client requested application version.")
        return ResponseEntity.ok().body(AppVersion(appVersion))
    }
}