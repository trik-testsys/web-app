package trik.testsys.webapp.notifier

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

/**
 * @author Viktor Karasev
 * @since %CURRENT_VERSION%
 */
@Component
class EmailIncidentNotifier(
    private val emailClient: EmailClient,
    @Value("\${trik.testsys.notifier.email.receivers}")
    private val receiversMail: String,
    @Value("\${spring.application.name}")
    private val appName: String,
): IncidentNotifier {

    private val mails = receiversMail.split(",")

    @PostConstruct
    fun init() {
        if (receiversMail.trim().isEmpty()) error("Email receivers must be initialized")

        logger.info("Initialized mails to be notified: $mails")
    }

    private val subject = "[ $appName ] TestSys incident"

    private fun sendMessage(body: String) {
        val message = EmailClient.Email(
            to = mails,
            subject = subject,
            body = body
        )
        emailClient.sendEmail(message)
    }

    override fun notify(msg: String) {
        sendMessage(msg)
    }

    override fun notify(msg: String, e: Exception) {
        val body = "${msg}\n${e.stackTraceToString()}"
        sendMessage(body)
    }

    companion object {

        private val logger = LoggerFactory.getLogger(EmailIncidentNotifier::class.java)
    }
}
