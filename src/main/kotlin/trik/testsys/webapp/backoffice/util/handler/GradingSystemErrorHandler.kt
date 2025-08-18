package trik.testsys.webapp.backoffice.util.handler

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.ResponseErrorHandler

import java.io.IOException
import java.net.URI


@Component
class GradingSystemErrorHandler : ResponseErrorHandler {

    @Throws(IOException::class)
    override fun hasError(httpResponse: ClientHttpResponse): Boolean {
        return (httpResponse.statusCode.is4xxClientError || httpResponse.statusCode.is5xxServerError)
    }

    @Throws(IOException::class)
    override fun handleError(url: URI, method: HttpMethod, httpResponse: ClientHttpResponse) {
        if (httpResponse.statusCode.is5xxServerError) {
            logger.warn("Grading system is not available. Status code: ${httpResponse.statusCode}")
            // handle SERVER_ERROR
            return
        }
        if (httpResponse.statusCode.is4xxClientError) {
            logger.warn("Client error. Status code: ${httpResponse.statusCode}")
            // handle CLIENT_ERROR
            return
        }
    }

    companion object {

        private val logger: Logger = LoggerFactory.getLogger(GradingSystemErrorHandler::class.java)
    }
}