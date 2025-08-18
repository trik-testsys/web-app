//package trik.testsys.webapp.backoffice.controller.impl.user.superuser
//
//import org.springframework.beans.factory.annotation.Qualifier
//import org.springframework.stereotype.Controller
//import org.springframework.ui.Model
//import org.springframework.web.bind.annotation.*
//import org.springframework.web.servlet.mvc.support.RedirectAttributes
//import trik.testsys.backoffice.controller.impl.main.LoginController
//import trik.testsys.backoffice.controller.impl.user.superuser.SuperUserMainController.Companion.SUPER_USER_PAGE
//import trik.testsys.backoffice.controller.impl.user.superuser.SuperUserMainController.Companion.SUPER_USER_PATH
//import trik.testsys.backoffice.controller.user.AbstractWebUserController
//import trik.testsys.backoffice.entity.user.WebUser
//import trik.testsys.backoffice.entity.user.impl.*
//import trik.testsys.backoffice.service.entity.user.impl.*
//import trik.testsys.backoffice.service.security.login.impl.LoginData
//import trik.testsys.backoffice.service.token.access.AccessTokenGenerator
//import trik.testsys.backoffice.service.token.reg.RegTokenGenerator
//import trik.testsys.backoffice.util.addPopupMessage
//import trik.testsys.backoffice.util.atTimeZone
//import trik.testsys.backoffice.view.impl.SuperUserView
//import trik.testsys.backoffice.view.impl.UserCreationView
//
///**
// * @author Roman Shishkin
// * @since 2.2.0
// */
//@Controller
//@RequestMapping(DeveloperUsersController.USERS_PATH)
//class DeveloperUsersController(
//    loginData: LoginData,
//
//    private val viewerService: ViewerService,
//    private val developerService: DeveloperService,
//    private val adminService: AdminService,
//    private val judgeService: JudgeService,
//    @Qualifier("webUserAccessTokenGenerator") private val webUserAccessTokenGenerator: AccessTokenGenerator,
//    @Qualifier("adminRegTokenGenerator") private val adminRegTokenGenerator: RegTokenGenerator,
//) : AbstractWebUserController<SuperUser, SuperUserView, SuperUserService>(loginData) {
//
//    override val mainPage = USERS_PAGE
//
//    override val mainPath = USERS_PATH
//
//    override fun SuperUser.toView(timeZoneId: String?) = SuperUserView(
//        id = this.id,
//        name = this.name,
//        accessToken = this.accessToken,
//        creationDate = this.creationDate?.atTimeZone(timeZoneId),
//        lastLoginDate = this.lastLoginDate?.atTimeZone(timeZoneId),
//        additionalInfo = this.additionalInfo,
//        viewers = viewerService.findAll().sortedBy { it.id },
//        developers = developerService.findAll().sortedBy { it.id },
//        admins = adminService.findAll().sortedBy { it.id },
//        judges = judgeService.findAll().sortedBy { it.id }
//    )
//
//    @GetMapping
//    fun usersGet(
//        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"
//
//        model.addAttribute(VIEWER_ATTR, UserCreationView.emptyViewer())
//        model.addAttribute(DEVELOPER_ATTR, UserCreationView.emptyDeveloper())
//        model.addAttribute(ADMIN_ATTR, UserCreationView.emptyAdmin())
//        model.addAttribute(JUDGE_ATTR, UserCreationView.emptyJudge())
//        model.addAttribute(WEB_USER_ATTR, webUser.toView(timezone))
//
//        return USERS_PAGE
//    }
//
//    @PostMapping("/create")
//    fun userPost(
//        @ModelAttribute("userCreationView") userCreationView: UserCreationView,
//        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): String {
//        loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"
//
//        val accessToken = webUserAccessTokenGenerator.generate(userCreationView.name)
//
//        when (userCreationView.type) {
//            WebUser.UserType.VIEWER -> {
//                val regToken = adminRegTokenGenerator.generate(userCreationView.name)
//                val viewer = Viewer(
//                    userCreationView.name, accessToken, regToken
//                ).also {
//                    it.additionalInfo = userCreationView.additionalInfo
//                }
//
//                viewerService.save(viewer)
//                redirectAttributes.addPopupMessage("Наблюдатель ${viewer.name} успешно создан.")
//            }
//            WebUser.UserType.DEVELOPER -> {
//                val developer = Developer(
//                    userCreationView.name, accessToken
//                ).also {
//                    it.additionalInfo = userCreationView.additionalInfo
//                }
//                developerService.save(developer)
//
//                redirectAttributes.addPopupMessage("Разработчик ${developer.name} успешно создан.")
//            }
//            WebUser.UserType.ADMIN -> {
//                val viewer = viewerService.find(userCreationView.viewerId) ?: run {
//                    redirectAttributes.addPopupMessage("Наблюдатель не найден.")
//                    return "redirect:$USERS_PATH"
//                }
//
//                val admin = Admin(
//                    userCreationView.name, accessToken
//                ).also {
//                    it.additionalInfo = userCreationView.additionalInfo
//                    it.viewer = viewer
//                }
//
//                adminService.save(admin)
//                redirectAttributes.addPopupMessage("Администратор ${admin.name} успешно создан.")
//            }
//            WebUser.UserType.JUDGE -> {
//                val judge = Judge(
//                    userCreationView.name, accessToken
//                ).also {
//                    it.additionalInfo = userCreationView.additionalInfo
//                }
//                judgeService.save(judge)
//
//                redirectAttributes.addPopupMessage("Судья ${judge.name} успешно создан.")
//            }
//            else -> {
//                redirectAttributes.addPopupMessage("Неизвестный тип пользователя.")
//            }
//        }
//
//        val anchor = when (userCreationView.type) {
//            WebUser.UserType.VIEWER -> "#viewers"
//            WebUser.UserType.DEVELOPER -> "#developers"
//            WebUser.UserType.ADMIN -> "#admins"
//            WebUser.UserType.JUDGE -> "#judges"
//            else -> ""
//        }
//
//        return "redirect:$USERS_PATH$anchor"
//    }
//
//    companion object {
//
//        const val USERS_PATH = "$SUPER_USER_PATH/users"
//        const val USERS_PAGE = "$SUPER_USER_PAGE/users"
//
//        const val VIEWER_ATTR = "viewer"
//        const val ADMIN_ATTR = "admin"
//        const val DEVELOPER_ATTR = "developer"
//        const val JUDGE_ATTR = "judge"
//    }
//}