package trik.testsys.webapp.backoffice.data.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.servlet.View
import trik.testsys.webapp.backoffice.data.entity.impl.RegToken
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.repository.UserRepository
import trik.testsys.webapp.backoffice.data.service.ViewerService
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class UserServiceImpl(
    private val accessTokenService: AccessTokenService, private val view: View
) :
    AbstractService<User, UserRepository>(),
    ViewerService {

    override fun createAdmin(regToken: RegToken): Boolean {
        val viewer = regToken.viewer ?: run {
            logger.warn("Could not create user with unassigned regToken(id=${regToken.id}).")
            return false
        }

        val accessToken = accessTokenService.generate(viewer.id)
        val admin = User().also {
            it.accessToken = accessToken
            it.viewer = viewer
            it.privileges.add(User.Privilege.ADMIN)

            viewer.managedAdmins.add(it)
        }

        accessTokenService.save(accessToken)
        saveAll(listOf(admin, viewer))
        return true
    }

    companion object {

        private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }
}