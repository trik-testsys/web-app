package trik.testsys.webapp.backoffice

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = ["trik.testsys.webapp.**"])
class Application {

    companion object {

        /**
         * @author Roman Shishkin
         * @since 3.12.0
         */
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}