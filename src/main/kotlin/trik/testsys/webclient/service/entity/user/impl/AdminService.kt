package trik.testsys.webclient.service.entity.user.impl

import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.repository.user.AdminRepository
import trik.testsys.webclient.repository.user.StudentRepository
import trik.testsys.webclient.service.entity.user.WebUserService

@Service
class AdminService(
    private val studentRepository: StudentRepository
) : WebUserService<Admin, AdminRepository>()