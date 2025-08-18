package trik.testsys.core.service.startup

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service

/**
 * Service which executes every [StartupRunner] implementation after application ready.
 *
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 **/
@Service
class StartupListener(
    context: ApplicationContext
) : ApplicationListener<ApplicationReadyEvent> {

    private val startUpRunnersMap = context.getBeansOfType(StartupRunner::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val startTime = System.currentTimeMillis()

        logger.info("Starting startup runners...")
        logger.info("Found ${startUpRunnersMap.size} startup runners.")

        startUpRunnersMap.forEach { (name, runner) ->
            logger.info("Starting runner $name...")
            runner.tryExecuteBlocking()
        }

        val endTime = System.currentTimeMillis()
        logger.info("All startup runners finished.")

        logger.info("Startup took ${(endTime - startTime) / 1000.0} seconds.")
    }

    private fun StartupRunner.tryExecuteBlocking() = try {
        executeBlocking()
    } catch (e: Exception) {
        logger.error("Error while running startup runner", e)
    } finally {
        logger.info("Startup runner finished.")
    }

    companion object {

        private val logger = LoggerFactory.getLogger(StartupListener::class.java)
    }
}