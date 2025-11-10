package trik.testsys.webapp.backoffice.service.impl

import net.axay.simplekotlinmail.delivery.MailerManager
import net.axay.simplekotlinmail.delivery.mailerBuilder
import net.axay.simplekotlinmail.delivery.sendSync
import net.axay.simplekotlinmail.email.emailBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class MailSender(
    @Value("\${trik.testsys.email-client.smtp.host}")
    private val smtpHost: String,
    @Value("\${trik.testsys.email-client.smtp.port}")
    private val smtpPort: Int,
    @Value("\${trik.testsys.email-client.domain}")
    private val emailDomain: String,
    @Value("\${trik.testsys.email-client.credentials.username}")
    private val username: String,
    @Value("\${trik.testsys.email-client.credentials.password}")
    private val password: String,
) {

    fun setUpMailer(
        host: String = smtpHost,
        port: Int = smtpPort,
        username: String = this.username,
        password: String = this.password
    ) {
        logger.info("Setting up mailer(host=$host, port=$port, username=$username, password=$password)")

        val mailer = mailerBuilder(host, port, username, password)
        MailerManager.defaultMailer = mailer

        logger.info("Mailer set up")
    }

    private suspend fun shutdownMailer() {
        logger.info("Shutting down mailer")
        MailerManager.shutdownMailers()
        logger.info("Mailer shut down")
    }


    suspend fun sendMail(
        from: String,
        to: String,
        subject: String,
        text: String,
        isHtml: Boolean = false
    ) {

        val email = emailBuilder {
            from("$from@$emailDomain")
            to(to)

            withSubject(subject)

            if (isHtml) withHTMLText(text)
            else withPlainText(text)
        }

        try {
            setUpMailer()
            email.sendSync()
            logger.debug("Success: Email sent to $to")
        } catch (e: Exception) {
            logger.debug("Failure: Failed to send email to $to. Cause: $e")
        } finally {
            shutdownMailer()
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(MailSender::class.java)


    }
}