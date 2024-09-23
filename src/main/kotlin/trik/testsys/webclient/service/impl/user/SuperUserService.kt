package trik.testsys.webclient.service.impl.user

import org.springframework.stereotype.Service
import trik.testsys.core.service.user.AbstractUserService
import trik.testsys.core.utils.marker.TrikService

import trik.testsys.webclient.entity.impl.user.SuperUser
import trik.testsys.webclient.repository.user.SuperUserRepository

@Service
class SuperUserService : AbstractUserService<SuperUser, SuperUserRepository>(), TrikService