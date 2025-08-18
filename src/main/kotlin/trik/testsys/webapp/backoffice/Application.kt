package trik.testsys.webapp.backoffice

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@SpringBootApplication(scanBasePackages = ["trik.testsys.*"])
class Application {

    companion object {

        /**
         * @author Roman Shishkin
         * @since %CURRENT_VERSION%
         */
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}