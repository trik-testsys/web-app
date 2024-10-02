package trik.testsys.webclient.service.token.access.impl

import org.springframework.stereotype.Service
import trik.testsys.webclient.service.token.AbstractTokenGenerator
import trik.testsys.webclient.service.token.access.AccessTokenGenerator

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Service("webUserAccessTokenGenerator")
class WebUserAccessTokenGenerator : AccessTokenGenerator, AbstractTokenGenerator(WEB_USER_ACCESS_TOKEN_PREFIX) {

    companion object {

        const val WEB_USER_ACCESS_TOKEN_PREFIX = "wu"
    }
}