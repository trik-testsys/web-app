package trik.testsys.webapp.backoffice.utils

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
fun <T : Any> getRedirection(httpStatus: HttpStatus, location: String): ResponseEntity<T> {
    return ResponseEntity.status(httpStatus)
        .header(HttpHeaders.LOCATION, location)
        .build()
}