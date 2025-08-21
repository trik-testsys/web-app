package trik.testsys.webapp.backoffice.data.service.impl

import org.springframework.stereotype.Service
import trik.testsys.webapp.backoffice.data.entity.impl.Verdict
import trik.testsys.webapp.backoffice.data.repository.VerdictRepository
import trik.testsys.webapp.backoffice.data.service.VerdictService
import trik.testsys.webapp.core.data.service.AbstractService

@Service
class VerdictServiceImpl :
    AbstractService<Verdict, VerdictRepository>(),
    VerdictService {

    override fun save(verdict: Verdict): Verdict = super.save(verdict)
}