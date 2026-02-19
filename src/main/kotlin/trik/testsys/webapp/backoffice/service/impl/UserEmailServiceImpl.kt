package trik.testsys.webapp.backoffice.service.impl

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.AccessToken
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.UserService
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService
import trik.testsys.webapp.backoffice.service.UserEmailService
import java.time.Instant
import java.util.UUID

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@Service
class UserEmailServiceImpl(
    private val emailClient: EmailClient,
//    private val mailSender: MailSender,
    private val userService: UserService,
    private val accessTokenService: AccessTokenService
) : UserEmailService {

    private val dataByVerificationToken = mutableMapOf<String, EmailData>()

    override fun sendVerificationToken(user: User, newEmail: String?) {
        val verificationToken = UUID.randomUUID().toString()

        val emailToSend = if (newEmail == null) {
            val prevEmail = user.email
            if (prevEmail == null) {
                logger.error("Trying to remove null email from user(id=${user.id})")
                return
            }

            logger.info("Removing email $prevEmail from user(id=${user.id})")

            prevEmail
        } else newEmail

        val data = EmailData(newEmail, requireNotNull(user.id))
        dataByVerificationToken[verificationToken] = data

        logger.debug("Sending verificationToken $verificationToken to email $emailToSend.")
        val email =
            if (newEmail == null) buildUnlinkVerificationEmail(emailToSend, verificationToken)
            else buildVerificationEmail(emailToSend, verificationToken)
        emailClient.sendEmail(email)
    }

    private fun buildUnlinkVerificationEmail(emailTo: String, verificationToken: String) = EmailClient.Email(
        FROM,
        listOf(emailTo),
        "Открепление почты на платформе TestSys",
        """
            Вы получили данное письмо, потому что эта почта была указана для восстановления доступа в тестирующей системе TestSys.
            Если это сделали не Вы – проигнорируйте данное сообщение.
            
            Код-подтверждения: $verificationToken
        """.trimIndent()
    )

    private fun buildVerificationEmail(emailTo: String, verificationToken: String) = EmailClient.Email(
        FROM,
        listOf(emailTo),
        "Подтверждение почты на платформе TestSys",
        """
            Вы получили данное письмо, потому что эта почта была указана для восстановления доступа в тестирующей системе TestSys.
            Если это сделали не Вы – проигнорируйте данное сообщение.
            
            Код-подтверждения: $verificationToken
        """.trimIndent()
    )

    override fun verify(user: User, verificationToken: String): Boolean {
        logger.info("Received verificationToken $verificationToken.")
        val data = dataByVerificationToken[verificationToken] ?: run {
            logger.info("No verification data where found by verificationToken $verificationToken")
            return false
        }

        if (data.userId != user.id) {
            logger.info("VerificationToken $verificationToken is not linked to user(id${user.id})")
            return false
        }
        dataByVerificationToken.remove(verificationToken)

        if (data.email == null) {
            logger.info("Removed email ${user.email} from user(id=${user.id})")
            userService.updateEmail(user, null)

            return true
        }

        logger.info("Verified new email ${data.email} on user(id=${user.id})")
        user.emailVerifiedAt = Instant.now()
        userService.save(user)

        return true
    }

    override fun sendAccessToken(email: String): Boolean {
        logger.info("Requested accessToken restore by email $email")

        val user = userService.findByEmail(email) ?: run {
            logger.info("User not found by email $email")
            return false
        }

        if (user.emailVerifiedAt == null) {
            logger.info("User(id=${user.id}) has not verified email $email")
            return false
        }

        val email = buildAccessTokenRestoreEmail(email, requireNotNull(user.accessToken!!.value))
        emailClient.sendEmail(email)
        return true
    }

    private fun buildAccessTokenRestoreEmail(emailTo: String, accessToken: String) = EmailClient.Email(
        FROM,
        listOf(emailTo),
        "Восстановление кода-доступа на платформе TestSys",
        """
            Вы получили данное письмо, потому что эта почта была указана для восстановления доступа в тестирующей системе TestSys.
            Если это сделали не Вы – проигнорируйте данное сообщение.
            
            Код-доступа: $accessToken
        """.trimIndent()
    )

    @Scheduled(fixedRate = 3_600_000)
    fun flushVerificationTokens() {
        logger.debug("Started flushing old verification tokens.")

        val now = Instant.now()
        val toRemove = mutableSetOf<String>()
        for ((token, data) in dataByVerificationToken) {
            if (data.createdAt.plusMillis(VERIFICATION_TOKEN_TTL).isAfter(now)) continue
            toRemove.add(token)
        }

        logger.debug("Found ${toRemove.size} verification tokens to be removed.")
        toRemove.forEach { token ->
            dataByVerificationToken.remove(token)
        }

        logger.debug("Finished flushing old verification tokens.")
    }

    private data class EmailData(
        val email: String?,
        val userId: Long,
        val createdAt: Instant = Instant.now()
    )

    companion object {

        private val logger = LoggerFactory.getLogger(UserEmailServiceImpl::class.java)

        private const val VERIFICATION_TOKEN_TTL = 60 * 60 * 1000L

        private const val FROM = "no-reply"
    }
}