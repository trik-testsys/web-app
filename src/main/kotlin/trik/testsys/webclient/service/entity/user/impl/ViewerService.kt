package trik.testsys.webclient.service.entity.user.impl

import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.repository.user.ViewerRepository
import trik.testsys.webclient.service.entity.user.WebUserService

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Service
class ViewerService : WebUserService<Viewer, ViewerRepository>() {

    fun findByAdminRegToken(adminRegToken: String): Viewer? {
        return repository.findByAdminRegToken(adminRegToken)
    }

    override fun validateName(entity: Viewer) =
        !entity.name.contains(entity.regToken, ignoreCase = true) && super.validateName(entity)

    override fun validateAdditionalInfo(entity: Viewer) =
        !entity.additionalInfo.contains(entity.regToken, ignoreCase = true) && super.validateAdditionalInfo(entity)
}