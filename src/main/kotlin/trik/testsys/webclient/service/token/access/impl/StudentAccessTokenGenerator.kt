package trik.testsys.webclient.service.token.access.impl

import org.springframework.stereotype.Service
import trik.testsys.webclient.service.token.AbstractTokenGenerator
import trik.testsys.webclient.service.token.access.AccessTokenGenerator

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Service("studentAccessTokenGenerator")
class StudentAccessTokenGenerator : AccessTokenGenerator, AbstractTokenGenerator(STUDENT_ACCESS_TOKEN_PREFIX) {

    companion object {

        const val STUDENT_ACCESS_TOKEN_PREFIX = "st"
    }
}