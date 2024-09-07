package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.impl.Judge
import trik.testsys.webclient.entity.impl.WebUser
import trik.testsys.webclient.repository.impl.JudgeRepository
import trik.testsys.webclient.service.TrikService

@Service
class JudgeService @Autowired constructor(
    private val judgeRepository: JudgeRepository
) : TrikService {

    fun save(webUser: WebUser) = judgeRepository.save(Judge(webUser))

    fun getByWebUser(webUser: WebUser) = judgeRepository.findByWebUser(webUser)
}