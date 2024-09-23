package trik.testsys.webclient.service.impl.user

import org.springframework.stereotype.Service
import trik.testsys.core.service.user.AbstractUserService
import trik.testsys.core.utils.marker.TrikService
import trik.testsys.webclient.entity.impl.user.Admin
import trik.testsys.webclient.repository.user.AdminRepository
import trik.testsys.webclient.repository.user.StudentRepository

@Service
class AdminService(
    private val studentRepository: StudentRepository
) : AbstractUserService<Admin, AdminRepository>(), TrikService