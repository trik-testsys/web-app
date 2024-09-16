package trik.testsys.webclient.service.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.user.AbstractUserService
import trik.testsys.core.utils.marker.TrikService
import trik.testsys.webclient.entity.impl.Judge
import trik.testsys.webclient.entity.impl.WebUser
import trik.testsys.webclient.repository.impl.JudgeRepository

@Service
class JudgeService : AbstractUserService<Judge, JudgeRepository>(), TrikService {

    fun save(webUser: WebUser) = repository.save(Judge(webUser))

    fun getByWebUser(webUser: WebUser) = repository.findByWebUser(webUser)
}