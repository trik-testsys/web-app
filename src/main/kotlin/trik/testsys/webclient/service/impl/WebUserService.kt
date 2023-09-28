package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import trik.testsys.webclient.entity.impl.WebUser
import trik.testsys.webclient.repository.impl.WebUserRepository
import trik.testsys.webclient.service.TrikService
import trik.testsys.webclient.util.AccessTokenGenerator

import java.security.MessageDigest
import java.util.Date
import java.util.Random

/**
 * @author Roman Shishkin
 * @since 1.0.0
 */
@Service
class WebUserService @Autowired constructor(
    private val webUserRepository: WebUserRepository
) : TrikService {

    fun saveWebUser(webUser: WebUser): WebUser {
        return webUserRepository.save(webUser)
    }

    fun saveWebUser(username: String): WebUser {
        val accessToken = AccessTokenGenerator.generateAccessToken(username, AccessTokenGenerator.TokenType.WEB_USER)
        val webUser = WebUser(username, accessToken)

        return webUserRepository.save(webUser)
    }

    fun saveWebUser(username: String, accessToken: String): WebUser {
        val webUser = WebUser(username, accessToken)

        return webUser
    }

    fun getWebUserByAccessToken(accessToken: String): WebUser? {
        return webUserRepository.findWebUserByAccessToken(accessToken)
    }

    fun getWebUserById(id: Long): WebUser? {
        return webUserRepository.findWebUserById(id)
    }
}