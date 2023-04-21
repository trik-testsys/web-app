package trik.testsys.webclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.ResponseErrorHandler
import java.io.IOException


@Component
class GradingSystemErrorHandler : ResponseErrorHandler {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Throws(IOException::class)
    override fun hasError(httpResponse: ClientHttpResponse): Boolean {
        return (httpResponse.statusCode.series() === HttpStatus.Series.CLIENT_ERROR
                || httpResponse.statusCode.series() === HttpStatus.Series.SERVER_ERROR)
    }

    @Throws(IOException::class)
    override fun handleError(httpResponse: ClientHttpResponse) {
        if (httpResponse.statusCode.series() === HttpStatus.Series.SERVER_ERROR) {
            logger.warn("Grading system is not available. Status code: ${httpResponse.statusCode}")
            // handle SERVER_ERROR
            return
        }
        if (httpResponse.statusCode.series() === HttpStatus.Series.CLIENT_ERROR) {
            logger.warn("Client error. Status code: ${httpResponse.statusCode}")
            // handle CLIENT_ERROR
            return
        }
    }
}