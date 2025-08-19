package trik.testsys.webapp.backoffice.data.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.RegToken
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.repository.UserRepository
import trik.testsys.webapp.backoffice.data.service.SuperUserService
import trik.testsys.webapp.backoffice.data.service.ViewerService
import trik.testsys.webapp.core.data.service.AbstractService

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class UserServiceImpl(
    private val accessTokenService: AccessTokenService,
    private val regTokenService: RegTokenService
) :
    AbstractService<User, UserRepository>(),
    ViewerService, SuperUserService {

    override fun createAdmin(regToken: RegToken): User? {
        val viewer = regToken.viewer ?: run {
            logger.warn("Could not create user with unassigned regToken(id=${regToken.id}).")
            return null
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
        return admin
    }

    override fun createUser(superUser: User, name: String, privileges: Collection<User.Privilege>): Boolean {
        if (!superUser.privileges.contains(User.Privilege.SUPER_USER)) {
            logger.warn(
                "Could not create new user(name=${name}, privileges=${privileges}) for user(id=${superUser.id}), " +
                        "it has no SuperUser privileges."
            )
            return false
        }

        val accessToken = accessTokenService.generate(superUser.id)
        val newUser = User().also {
            it.superUser = superUser

            it.accessToken = accessToken
            accessToken.user = it

            addPrivileges(superUser, it, privileges)
            superUser.createdUsers.add(it)
        }

        saveAll(listOf(newUser, superUser))
        accessTokenService.save(accessToken)

        return true
    }

    override fun addPrivilege(superUser: User, user: User, privilege: User.Privilege): Boolean {
        if (!superUser.privileges.contains(User.Privilege.SUPER_USER)) {
            logger.warn(
                "Could not add privilege(privilege=$privilege) to user(id=${user.id}) by user(id=${superUser.id}), " +
                        "it has no SuperUser privileges."
            )
            return false
        }

        if (user.privileges.contains(privilege)) {
            logger.warn("Could not add already present privilege(privilege=$privilege) to user(id=${user.id}).")
            return false
        }

        logger.debug("Adding privilege(privilege=$privilege) to user(id=${user.id}).")
        user.privileges.add(privilege)

        if (privilege == User.Privilege.VIEWER && user.adminRegToken == null) {
            logger.info("User(id=${user.id}) granted VIEWER. Generating adminRegToken.")

            val regToken = regTokenService.generate(superUser.id)
            user.adminRegToken = regToken
            regToken.viewer = user
            regTokenService.save(regToken)
        }

        save(user)
        return true
    }

    companion object {

        private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }
}