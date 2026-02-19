package trik.testsys.webapp.backoffice.utils

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
fun <T : Any> getRedirection(httpStatus: HttpStatus, location: String): ResponseEntity<T> {
    return ResponseEntity.status(httpStatus)
        .header(HttpHeaders.LOCATION, location)
        .build()
}