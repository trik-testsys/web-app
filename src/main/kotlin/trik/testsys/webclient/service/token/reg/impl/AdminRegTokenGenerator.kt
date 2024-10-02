package trik.testsys.webclient.service.token.reg.impl

import org.springframework.stereotype.Service
import trik.testsys.webclient.service.token.AbstractTokenGenerator
import trik.testsys.webclient.service.token.reg.RegTokenGenerator

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Service("adminRegTokenGenerator")
class AdminRegTokenGenerator : RegTokenGenerator, AbstractTokenGenerator(ADMIN_REG_TOKEN_PREFIX) {


    companion object {

        const val ADMIN_REG_TOKEN_PREFIX = "adm"
    }
}