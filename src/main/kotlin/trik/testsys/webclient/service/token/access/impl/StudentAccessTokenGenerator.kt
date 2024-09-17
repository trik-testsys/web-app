package trik.testsys.webclient.service.token.access.impl

import org.springframework.stereotype.Service
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.service.token.access.AccessTokenGenerator
import java.util.*

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Service("studentAccessTokenGenerator")
class StudentAccessTokenGenerator : AccessTokenGenerator {

    // generate token like a61d-e3f2-4b3a-8b1c
    override fun generate(string: String): AccessToken {
        val uuid = UUID.randomUUID().toString()
        val middleUUID = uuid.substringAfter("-").substringBeforeLast("-")

        val number = string.charsSum() % 10000
        val numberString = number.toString().padStart(4, '0')

        return "$numberString-$middleUUID"
    }

    private fun String.charsSum() = fold(0) { sum, char -> sum + char.code }
}