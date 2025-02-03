package trik.testsys.webclient.service.startup.runner.impl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.service.entity.impl.GroupService
import trik.testsys.webclient.service.entity.user.impl.AdminService
import trik.testsys.webclient.service.entity.user.impl.DeveloperService
import trik.testsys.webclient.service.entity.user.impl.ViewerService
import trik.testsys.webclient.service.startup.runner.StartupRunner
import trik.testsys.webclient.service.token.reg.RegTokenGenerator

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class LektoriumEntityCreatorRunner(
    @Value("\${lektorium.groupRegToken}") val groupRegToken: AccessToken,
    @Value("\${lektorium.adminToken}") val adminToken: AccessToken,
    @Value("\${lektorium.viewerToken}") val viewerToken: AccessToken,
    @Value("\${lektorium.developerToken}") val developerToken: AccessToken,

    private val groupService: GroupService,
    private val developerService: DeveloperService,
    private val viewerService: ViewerService,
    private val adminService: AdminService,

    @Qualifier("adminRegTokenGenerator")
    private val adminRegTokenGenerator: RegTokenGenerator,
) : StartupRunner {

    override fun runBlocking() {
        if (hasLektoriumInitialized()) {
            return
        }

        val viewer = createViewer()
        val admin = createAdmin(viewer)
        val group = createGroup(admin)
        val developer = createDeveloper()
    }

    override suspend fun run() {
        TODO("Not yet implemented")
    }

    private fun hasLektoriumInitialized(): Boolean {
        return viewerService.findByAccessToken(viewerToken) != null
    }

    private fun createViewer(): Viewer {
        val adminRegToken = adminRegTokenGenerator.generate(viewerToken)
        val viewer = Viewer("Lektorium Viewer", viewerToken, adminRegToken)

        return viewerService.save(viewer)
    }

    private fun createAdmin(viewer: Viewer): Admin {
        val admin = Admin("Lektorium Admin", adminToken).also {
            it.viewer = viewer
        }

        return adminService.save(admin)
    }

    private fun createGroup(admin: Admin): Group {
        val group = Group("Lektorium Group", groupRegToken).also {
            it.admin = admin
        }

        return groupService.save(group)
    }

    private fun createDeveloper(): Developer {
        val developer = Developer("Lektorium Developer", developerToken)

        return developerService.save(developer)
    }
}