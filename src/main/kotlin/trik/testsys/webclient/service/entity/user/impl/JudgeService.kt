package trik.testsys.webclient.service.entity.user.impl

import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.user.impl.Judge
import trik.testsys.webclient.repository.user.JudgeRepository
import trik.testsys.webclient.service.entity.user.WebUserService

@Service
class JudgeService : WebUserService<Judge, JudgeRepository>()