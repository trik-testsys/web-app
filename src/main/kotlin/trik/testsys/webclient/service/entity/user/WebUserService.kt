package trik.testsys.webclient.service.entity.user

import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.service.user.AbstractUserService
import trik.testsys.webclient.entity.user.WebUser

/**
 * @author Roman Shishkin
 * @since 2.0.0
**/
abstract class WebUserService<E : WebUser, R : UserRepository<E>> : AbstractUserService<E, R>()