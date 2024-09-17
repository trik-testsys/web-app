package trik.testsys.webclient.repository.user

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.utils.marker.TrikRepository
import trik.testsys.webclient.entity.impl.user.Judge
import trik.testsys.webclient.entity.impl.user.WebUser

@Repository
interface JudgeRepository : UserRepository<Judge>, TrikRepository {

    fun findByWebUser(webUser: WebUser): Judge?
}