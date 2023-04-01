package trik.testsys.webclient.controllers.basic

import com.beust.klaxon.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class VersionController(@Value("\${app.version}") final val appVersion: String) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val appVersionJson = JsonObject(
        mapOf(
            "version" to appVersion
        )
    )

    @GetMapping("/version")
    fun getVersion(): ResponseEntity<JsonObject> {
        logger.info("Client requested application version.")
        return ResponseEntity.ok().body(appVersionJson)
    }
}