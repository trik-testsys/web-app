package trik.testsys.webclient.service.startup.impl

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
import trik.testsys.webclient.service.startup.runner.StartupRunner

/**
 * @author Roman Shishkin
 * @since 2.0.0
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

            runner.tryRun()
        }

        val endTime = System.currentTimeMillis()
        logger.info("All startup runners finished.")

        logger.info("Startup took ${(endTime - startTime) / 1000.0} seconds.")
    }

    private fun StartupRunner.tryRun() = try {
        runBlocking()
    } catch (e: Exception) {
        logger.error("Error while running startup runner", e)
    } finally {
        logger.info("Startup runner finished.")
    }

    companion object {

        private val logger = LoggerFactory.getLogger(StartupListener::class.java)
    }
}