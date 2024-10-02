package trik.testsys.webclient.service.token


/**
 * Interface for token generator services.
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface TokenGenerator {

    /**
     * Generates token by input [string].
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun generate(string: String): String
}