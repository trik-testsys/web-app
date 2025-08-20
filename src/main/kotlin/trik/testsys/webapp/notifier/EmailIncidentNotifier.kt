package trik.testsys.webapp.notifier

/**
 * @author Viktor Karasev
 * @since %CURRENT_VERSION%
 */
class EmailIncidentNotifier(
    private val emailClient: EmailClient,
    private val receiversMail: List<String>
): IncidentNotifier {

    private val subject = "TestSys incident"

    private fun sendMessage(body: String) {
        val message = EmailClient.Email(
            to = receiversMail,
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

}
