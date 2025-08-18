package trik.testsys.core.service.startup

import kotlinx.coroutines.runBlocking

/**
 * Interface for services, which should be executed exactly when application is ready.
 *
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 **/
interface StartupRunner {

    /**
     * Entry point to execute [StartupRunner] (suspend).
     *
     * @author Roman Shishkin
     * @since %CURRENT_VERSION%
     */
    suspend fun execute()

    /**
     * Entry point to execute [StartupRunner].
     *
     * @author Roman Shishkin
     * @since %CURRENT_VERSION%
     */
    fun executeBlocking() = runBlocking { execute() }
}