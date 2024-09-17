package trik.testsys.webclient.service.token.reg.impl

import org.springframework.stereotype.Service
import trik.testsys.webclient.service.token.reg.RegTokenGenerator
import java.security.MessageDigest
import java.util.*

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Service("groupRegTokenGenerator")
class GroupRegTokenGenerator : RegTokenGenerator {

    override fun generate(string: String): String {
        val saltedWord = string + Date().time + Random(Date().time).nextInt()
        val md = MessageDigest.getInstance("SHA-224")
        val digest = md.digest(saltedWord.toByteArray())

        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}