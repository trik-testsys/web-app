package trik.testsys.webclient.controller.impl.user.admin

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.impl.user.admin.AdminGroupController.Companion.GROUP_PATH
import trik.testsys.webclient.controller.impl.user.admin.AdminGroupsController.Companion.GROUPS_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.service.UserAgentParser
import trik.testsys.webclient.service.entity.impl.ContestService
import trik.testsys.webclient.service.entity.impl.GroupService
import trik.testsys.webclient.service.entity.user.impl.AdminService
import trik.testsys.webclient.service.entity.user.impl.StudentService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.service.token.reg.RegTokenGenerator
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.view.impl.AdminView
import trik.testsys.webclient.view.impl.ContestView.Companion.toView
import trik.testsys.webclient.view.impl.GroupCreationView
import trik.testsys.webclient.view.impl.GroupView
import trik.testsys.webclient.view.impl.GroupView.Companion.toView
import trik.testsys.webclient.view.impl.StudentView.Companion.toView
import java.util.*
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping(GROUP_PATH)
class AdminGroupController(
    loginData: LoginData,

    private val groupService: GroupService,
    @Qualifier("groupRegTokenGenerator") private val groupRegTokenGenerator: RegTokenGenerator,

    private val contestService: ContestService,
    private val studentService: StudentService,
    private val userAgentParser: UserAgentParser
) : AbstractWebUserController<Admin, AdminView, AdminService>(loginData) {

    override val mainPage = GROUP_PAGE

    override val mainPath = GROUP_PATH

    override fun Admin.toView(timeZoneId: String?) = TODO()

    @PostMapping("/create")
    fun groupPost(
        @ModelAttribute("group") groupView: GroupCreationView,
        timeZone: TimeZone,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        val regToken = groupRegTokenGenerator.generate(groupView.name)
        val group = groupView.toEntity(regToken, webUser)

        groupService.validate(group, redirectAttributes, "redirect:$GROUPS_PATH")?.let { return it }

        groupService.save(group)

        redirectAttributes.addPopupMessage("Группа ${group.name} успешно создана.")

        return "redirect:$GROUPS_PATH"
    }

    @GetMapping("/{groupId}")
    fun groupGet(
        @PathVariable("groupId") id: Long,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (!webUser.validateGroupExistence(id)) {
            redirectAttributes.addPopupMessage("Группа с ID $id не найдена.")
            return "redirect:$GROUPS_PATH"
        }

        val group = groupService.find(id) ?: run {
            redirectAttributes.addPopupMessage("Группа с ID $id не найдена.")
            return "redirect:$GROUPS_PATH"
        }

        val groupView = group.toView(timezone)
        model.addAttribute(GROUP_ATTR, groupView)

        val publicContests = contestService.findAllPublic()
        val linkedContests = group.contests
        val unLinkedContests = publicContests.filter { it !in linkedContests }.toSet()

        model.addAttribute(LINKED_CONTESTS_ATTR, linkedContests.map { it.toView(timezone) }.sortedBy { it.id })
        model.addAttribute(UNLINKED_CONTESTS_ATTR, unLinkedContests.map { it.toView(timezone) }.sortedBy { it.id })
        model.addAttribute(STUDENTS_ATTR, group.students.map { it.toView(timezone) }.sortedBy { it.id })

        return GROUP_PAGE
    }

    @PostMapping("/generateStudents/{groupId}")
    fun groupGenerateStudents(
        @PathVariable("groupId") groupId: Long,
        @RequestParam("count") count: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (!webUser.validateGroupExistence(groupId)) {
            redirectAttributes.addPopupMessage("Группа с ID $groupId не найдена.")
            return "redirect:$GROUPS_PATH"
        }

        val group = groupService.find(groupId) ?: run {
            redirectAttributes.addPopupMessage("Группа с ID $groupId не найдена.")
            return "redirect:$GROUPS_PATH"
        }

        if (count < 1) {
            redirectAttributes.addPopupMessage("Количество Участников должно быть больше 0.")
            return "redirect:$GROUP_PATH/$groupId"
        }

        val students = studentService.generate(count, group)

        group.students.addAll(students)

        redirectAttributes.addPopupMessage("Сгенерировано $count Участников.")

        return "redirect:$GROUP_PATH/$groupId"
    }

    @GetMapping("/exportStudents/{groupId}")
    fun groupExportStudents(
        @PathVariable("groupId") groupId: Long,
        @RequestHeader("User-Agent") userAgent: String,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): Any {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (!webUser.validateGroupExistence(groupId)) {
            redirectAttributes.addPopupMessage("Группа с ID $groupId не найдена.")
            return "redirect:$GROUPS_PATH"
        }

        val group = groupService.find(groupId) ?: run {
            redirectAttributes.addPopupMessage("Группа с ID $groupId не найдена.")
            return "redirect:$GROUPS_PATH"
        }

        val students = group.students.sortedBy { it.id }

        val filename = "students_${System.currentTimeMillis()}.csv"
        val contentDisposition = "attachment; filename=$filename"

        val csv = students.joinToString("\n") { "${it.id};${it.name};${it.accessToken}" }
        val charset = userAgentParser.getCharset(userAgent)
        val bytes = csv.toByteArray(charset)

        val responseEntity = ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
            .header(HttpHeaders.CONTENT_ENCODING, charset.name())
            .contentType(MediaType.TEXT_PLAIN)
            .body(bytes)

        redirectAttributes.addPopupMessage("Студенты успешно экспортированы.")

        return responseEntity
    }

    @PostMapping("/linkContest/{groupId}")
    fun groupLinkContest(
        @PathVariable("groupId") groupId: Long,
        @RequestParam("contestId") contestId: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (!webUser.validateGroupExistence(groupId)) {
            redirectAttributes.addPopupMessage("Группа с ID $groupId не найдена.")
            return "redirect:$GROUPS_PATH"
        }

        val group = groupService.find(groupId) ?: run {
            redirectAttributes.addPopupMessage("Группа с ID $groupId не найдена.")
            return "redirect:$GROUPS_PATH"
        }

        val contest = contestService.find(contestId) ?: run {
            redirectAttributes.addPopupMessage("Тур с ID $contestId не найден.")
            return "redirect:$GROUP_PATH/$groupId"
        }

        if (!contest.isPublic()) {
            redirectAttributes.addPopupMessage("Тур с ID $contestId не найден.")
            return "redirect:$GROUP_PATH/$groupId"
        }

        group.contests.add(contest)
        groupService.save(group)
        contest.groups.add(group)
        contestService.save(contest)

        redirectAttributes.addPopupMessage("Тур ${contest.name} успешно привязан к группе ${group.name}.")

        return "redirect:$GROUP_PATH/$groupId"
    }

    @PostMapping("/unlinkContest/{groupId}")
    fun groupUnlinkContest(
        @PathVariable("groupId") groupId: Long,
        @RequestParam("contestId") contestId: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (!webUser.validateGroupExistence(groupId)) {
            redirectAttributes.addPopupMessage("Группа с ID $groupId не найдена.")
            return "redirect:$GROUPS_PATH"
        }

        val group = groupService.find(groupId) ?: run {
            redirectAttributes.addPopupMessage("Группа с ID $groupId не найдена.")
            return "redirect:$GROUPS_PATH"
        }

        val contest = contestService.find(contestId) ?: run {
            redirectAttributes.addPopupMessage("Тур с ID $contestId не найден.")
            return "redirect:$GROUP_PATH/$groupId"
        }

        if (!contest.isPublic()) {
            redirectAttributes.addPopupMessage("Тур с ID $contestId не найден.")
            return "redirect:$GROUP_PATH/$groupId"
        }

        contest.groups.remove(group)
        contestService.save(contest)

        group.contests.remove(contest)
        groupService.save(group)

        redirectAttributes.addPopupMessage("Тур ${contest.name} успешно откреплен от группы ${group.name}.")

        return "redirect:$GROUP_PATH/$groupId"
    }

    @PostMapping("/update/{groupId}")
    fun groupUpdate(
        @PathVariable("groupId") groupId: Long,
        @ModelAttribute("group") groupView: GroupView,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:${LoginController.LOGIN_PATH}"

        if (!webUser.validateGroupExistence(groupId)) {
            redirectAttributes.addPopupMessage("Группа с ID $groupId не найдена.")
            return "redirect:$GROUPS_PATH"
        }

        val group = groupView.toEntity(timezone)
        group.admin = webUser

        groupService.validate(group, redirectAttributes, "redirect:$GROUP_PATH/$groupId")?.let { return it }

        val updatedGroup = groupService.save(group)

        model.addAttribute(GROUP_ATTR, updatedGroup.toView(timezone))
        redirectAttributes.addPopupMessage("Данные успешно изменены.")

        return "redirect:$GROUP_PATH/$groupId"
    }

    companion object {

        const val GROUP_PATH = "$GROUPS_PATH/group"
        const val GROUP_PAGE = "admin/group"

        const val GROUP_ATTR = "group"

        const val STUDENTS_ATTR = "students"

        const val LINKED_CONTESTS_ATTR = "linkedContests"
        const val UNLINKED_CONTESTS_ATTR = "unlinkedContests"

        fun Admin.validateGroupExistence(groupId: Long?) = groups.any { it.id == groupId }

        fun GroupService.validate(group: Group, redirectAttributes: RedirectAttributes, redirect: String): String? {
            if (!validateName(group)) {
                redirectAttributes.addPopupMessage("Название группы не должно содержать Код-доступа.")
                return redirect
            }

            if (!validateAdditionalInfo(group)) {
                redirectAttributes.addPopupMessage("Дополнительная информация не должна содержать Код-доступа.")
                return redirect
            }

            return null
        }
    }
}