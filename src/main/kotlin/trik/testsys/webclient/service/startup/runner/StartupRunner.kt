package trik.testsys.webclient.service.startup.runner

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
interface StartupRunner {

    fun runBlocking()

    suspend fun run()
}