package trik.testsys.webclient.utils.logger

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Roman Shishkin
 * @since 1.1.0
 *
 * Implementation of custom logger from Trik TestSys components. Uses [Logger].
 */
class TrikLogger<T>(clazz: Class<T>) {

    private val logger = LoggerFactory.getLogger(clazz)

    fun info(message: String) = logger.info(message)

    fun warn(message: String) = logger.warn(message)

    fun error(message: String) = logger.error(message)

    fun info(userAccessToken: String, message: String) {
        val fullMessage = createFullMessage(userAccessToken, message)

        logger.info(fullMessage)
    }

    fun warn(userAccessToken: String, message: String) {
        val fullMessage = createFullMessage(userAccessToken, message)

        logger.warn(fullMessage)
    }

    fun error(userAccessToken: String, message: String) {
        val fullMessage = createFullMessage(userAccessToken, message)

        logger.error(fullMessage)
    }

    /**
     * Creates new full message using user access token as prefix in pattern:
     *
     * "[ {([MAX_TOKEN_LENGTH] - [userAccessToken].length) * {" "}} {[userAccessToken]} ]: {[message]}"
     */
    private fun createFullMessage(userAccessToken: String, message: String): String {
        val accessTokenWithBrackets = "[ ${userAccessToken.padStart(MAX_TOKEN_LENGTH)} ]"

        return "$accessTokenWithBrackets: $message"
    }

    companion object {
        private const val MAX_TOKEN_LENGTH = 80
    }
}