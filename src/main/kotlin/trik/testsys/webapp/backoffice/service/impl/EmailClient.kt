package trik.testsys.webapp.backoffice.service.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Properties
import javax.annotation.PostConstruct
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * @author Viktor Karasev
 * @since %CURRENT_VERSION%
 */
@Service
class EmailClient(
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

    @PostConstruct
    fun init() {
        if (smtpHost.trim().isEmpty()) error("smtp host must be initialized")
        if (smtpPort == null) error("smtp port must be initialized")
        if (emailDomain.trim().isEmpty()) error("Email domain must be initialized")
        if (username.trim().isEmpty()) error("Email username must be initialized")
        if (password.trim().isEmpty()) error("Email password must be initialized")
    }

    private val session: Session by lazy {
        val properties = Properties().apply {
            put("mail.smtp.host", smtpHost)
            put("mail.smtp.port", smtpPort.toString())
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.socketFactory.port", smtpPort.toString())
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")

        }

        Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })
    }

    data class Email(
        val from: String,
        val to: List<String>,
        val subject: String,
        val body: String,
        val cc: List<String> = emptyList(),
        val bcc: List<String> = emptyList()
    )

    fun sendEmail(email: Email) {
        return try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress("${email.from}@$emailDomain"))
                email.to.forEach { addRecipient(Message.RecipientType.TO, InternetAddress(it)) }
                email.cc.forEach { addRecipient(Message.RecipientType.CC, InternetAddress(it)) }
                email.bcc.forEach { addRecipient(Message.RecipientType.BCC, InternetAddress(it)) }
                subject = email.subject
                setText(email.body)
            }
            Transport.send(message)

            logger.info("Email message sent.")
        } catch (e: Exception) {
            logger.error("Failed to send email: ", e)
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(EmailClient::class.java)
    }
}