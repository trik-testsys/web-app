package trik.testsys.webclient.service.token.access.impl

import org.springframework.stereotype.Service
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.service.token.access.AccessTokenGenerator
import java.security.MessageDigest
import java.util.*

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Service("webUserAccessTokenGenerator")
class WebUserAccessTokenGenerator : AccessTokenGenerator {

    override fun generate(string: String): AccessToken {
        val saltedWord = string + Date().time + Random(Date().time).nextInt()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(saltedWord.toByteArray())

        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}