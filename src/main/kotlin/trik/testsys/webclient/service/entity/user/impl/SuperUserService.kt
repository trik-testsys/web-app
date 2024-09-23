package trik.testsys.webclient.service.entity.user.impl

import org.springframework.stereotype.Service
import trik.testsys.core.utils.marker.TrikService
import trik.testsys.webclient.entity.user.impl.SuperUser
import trik.testsys.webclient.repository.user.SuperUserRepository
import trik.testsys.webclient.service.entity.user.WebUserService

@Service
class SuperUserService : WebUserService<SuperUser, SuperUserRepository>(), TrikService