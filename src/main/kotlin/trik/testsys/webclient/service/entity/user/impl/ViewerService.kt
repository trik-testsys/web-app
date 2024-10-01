package trik.testsys.webclient.service.entity.user.impl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.repository.user.ViewerRepository
import trik.testsys.webclient.service.entity.RegEntityService
import trik.testsys.webclient.service.entity.user.WebUserService
import trik.testsys.webclient.service.token.access.AccessTokenGenerator

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Service
class ViewerService(
    private val adminService: AdminService,
    @Qualifier("webUserAccessTokenGenerator") private val webUserAccessTokenGenerator: AccessTokenGenerator
) :
    RegEntityService<Viewer, Admin>,
    WebUserService<Viewer, ViewerRepository>() {

    override fun findByRegToken(regToken: AccessToken) = repository.findByRegToken(regToken)

    override fun register(regToken: AccessToken, name: String): Admin? {
        val viewer = findByRegToken(regToken) ?: return null

        val accessToken = webUserAccessTokenGenerator.generate(name)
        val admin = Admin(name, accessToken)
        admin.viewer = viewer

        return admin.takeIf { adminService.validateName(it) }?.also { adminService.save(it) }
    }

    override fun validateAdditionalInfo(entity: Viewer) =
        super<RegEntityService>.validateAdditionalInfo(entity) && super<WebUserService>.validateAdditionalInfo(entity)

    override fun validateName(entity: Viewer) =
        super<RegEntityService>.validateName(entity) && super<WebUserService>.validateName(entity)
}