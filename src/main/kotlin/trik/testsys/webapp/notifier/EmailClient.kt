package trik.testsys.webapp.notifier

import java.util.Properties
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
class EmailClient(
    private val smtpHost: String,
    private val smtpPort: Int,
    private val username: String,
    private val password: String,
) {

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
        val to: List<String>,
        val subject: String,
        val body: String,
        val cc: List<String> = emptyList(),
        val bcc: List<String> = emptyList()
    )

    fun sendEmail(email: Email) {
        return try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                email.to.forEach { addRecipient(Message.RecipientType.TO, InternetAddress(it)) }
                email.cc.forEach { addRecipient(Message.RecipientType.CC, InternetAddress(it)) }
                email.bcc.forEach { addRecipient(Message.RecipientType.BCC, InternetAddress(it)) }
                subject = email.subject
                setText(email.body)
            }
            Transport.send(message)
            println("Message sent")// TODO: log error
        } catch (e: Exception) {
            println("Failed to send email: ${e.message}\n${e.stackTraceToString()}") // TODO: log error
        }
    }
}
