package trik.testsys.webclient.service.token

import trik.testsys.core.entity.user.AccessToken
import java.util.*

/**
 * @author Roman Shishkin
 * @since 2.0.0
**/
abstract class AbstractTokenGenerator(
    private val prefix: String = ""
) : TokenGenerator {

    // generate token like a61d-e3f2-4b3a-8b1c
    override fun generate(string: String): AccessToken {
        val uuid = UUID.randomUUID().toString()
        val middleUUID = uuid.substringAfter("-").substringBeforeLast("-")

        val number = string.charsSum() % 10000
        val numberString = number.toString().padStart(4, '0')

        return "$prefix-$numberString-$middleUUID"
    }

    private fun String.charsSum() = fold(0) { sum, char -> sum + char.code }
}