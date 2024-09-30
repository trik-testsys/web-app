package trik.testsys.webclient.service.entity.user.impl

import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.repository.user.AdminRepository
import trik.testsys.webclient.repository.user.StudentRepository
import trik.testsys.webclient.service.entity.user.WebUserService

@Service
class AdminService(
    private val studentRepository: StudentRepository
) : WebUserService<Admin, AdminRepository>() {

    override fun validateName(entity: Admin) =
        !entity.name.contains(entity.viewer.regToken) && super.validateName(entity)

    override fun validateAdditionalInfo(entity: Admin) =
        !entity.additionalInfo.contains(entity.viewer.regToken) && super.validateAdditionalInfo(entity)
}