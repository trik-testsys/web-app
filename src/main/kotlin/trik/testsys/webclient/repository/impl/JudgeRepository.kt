package trik.testsys.webclient.repository.impl

import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.impl.Judge
import trik.testsys.webclient.entity.impl.WebUser
import trik.testsys.webclient.repository.TrikRepository

@Repository
interface JudgeRepository : TrikRepository<Judge> {

    fun findByWebUser(webUser: WebUser): Judge?
}