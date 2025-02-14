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
import trik.testsys.webclient.service.token.access.AccessTokenGenerator
import trik.testsys.webclient.service.token.reg.RegTokenGenerator

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class LektoriumEntityCreatorRunner(
    @Value("\${create-lektorium-users}") private val createLektoriumUsers: Boolean,
    @Value("\${lektorium-group-reg-token}") private val groupRegToken: AccessToken,

    private val groupService: GroupService,
    private val developerService: DeveloperService,
    private val viewerService: ViewerService,
    private val adminService: AdminService,

    @Qualifier("adminRegTokenGenerator") private val adminRegTokenGenerator: RegTokenGenerator,
    @Qualifier("webUserAccessTokenGenerator") private val webUserAccessTokenGenerator: AccessTokenGenerator,
) : StartupRunner {

    override fun runBlocking() {
        if (!createLektoriumUsers || hasLektoriumInitialized()) {
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
        return viewerService.findAll().any { it.accessToken.startsWith(LEKTORIUM_PREFIX) }
    }

    private fun createViewer(): Viewer {
        val adminRegToken = adminRegTokenGenerator.generate(LEKTORIUM_PREFIX)
        val viewerToken = webUserAccessTokenGenerator.generate(LEKTORIUM_PREFIX)
        val viewer = Viewer("Lektorium Viewer", "$LEKTORIUM_PREFIX-$viewerToken", adminRegToken)

        return viewerService.save(viewer)
    }

    private fun createAdmin(viewer: Viewer): Admin {
        val adminToken = webUserAccessTokenGenerator.generate(LEKTORIUM_PREFIX)
        val admin = Admin("Lektorium Admin", "$LEKTORIUM_PREFIX-$adminToken").also {
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
        val developerToken = webUserAccessTokenGenerator.generate(LEKTORIUM_PREFIX)
        val developer = Developer("Lektorium Developer", "$LEKTORIUM_PREFIX-$developerToken")

        return developerService.save(developer)
    }

    companion object {

        private const val LEKTORIUM_PREFIX = "lkt"
    }
}