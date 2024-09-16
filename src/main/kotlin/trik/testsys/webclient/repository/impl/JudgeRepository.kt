package trik.testsys.webclient.repository.impl

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.utils.marker.TrikRepository
import trik.testsys.webclient.entity.impl.Judge
import trik.testsys.webclient.entity.impl.WebUser

@Repository
interface JudgeRepository : UserRepository<Judge>, TrikRepository {

    fun findByWebUser(webUser: WebUser): Judge?
}