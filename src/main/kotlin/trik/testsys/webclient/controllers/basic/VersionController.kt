package trik.testsys.webclient.controllers.basic

import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class VersionController(@Value("\${app.version}") val appVersion: String) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/version", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getVersion(): ResponseEntity<Map<String, String>> {
        logger.info("Client requested application version.")
        val appVersion = mapOf("version" to appVersion)

        return ResponseEntity.ok().body(appVersion)
    }
}