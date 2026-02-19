package trik.testsys.webapp.backoffice

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = ["trik.testsys.webapp.**"])
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