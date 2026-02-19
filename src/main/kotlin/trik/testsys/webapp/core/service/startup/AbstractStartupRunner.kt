package trik.testsys.webapp.core.service.startup

import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener

/**
 * Interface for services, which should be executed exactly when application is ready.
 *
 * @author Roman Shishkin
 * @since 3.12.0
 **/
abstract class AbstractStartupRunner : ApplicationListener<ApplicationReadyEvent> {

    protected val logger: Logger by lazy { LoggerFactory.getLogger(javaClass) }

    override fun onApplicationEvent(event: ApplicationReadyEvent) = try {
        logger.info("Executing ${javaClass.simpleName} startup runner.")
        executeBlocking()
    } catch (e: Exception) {
        logger.error("Error while running startup runner", e)
    } finally {
        logger.info("Startup runner finished.")
    }

    /**
     * Entry point to execute [AbstractStartupRunner] (suspend).
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    abstract suspend fun execute()

    /**
     * Entry point to execute [AbstractStartupRunner].
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    fun executeBlocking() = runBlocking { execute() }
}