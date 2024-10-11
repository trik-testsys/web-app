package trik.testsys.webclient.controller.impl.user.developer

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.user.developer.DeveloperContestController.Companion.CONTEST_PATH
import trik.testsys.webclient.controller.impl.user.developer.DeveloperContestsController.Companion.CONTESTS_PATH
import trik.testsys.webclient.controller.impl.user.developer.DeveloperMainController.Companion.DEVELOPER_PAGE
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.controller.user.AbstractWebUserMainController.Companion.LOGIN_PATH
import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.service.entity.impl.ContestService
import trik.testsys.webclient.service.entity.user.impl.DeveloperService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.view.impl.ContestCreationView
import trik.testsys.webclient.view.impl.ContestView
import trik.testsys.webclient.view.impl.ContestView.Companion.toView
import trik.testsys.webclient.view.impl.DeveloperView
import java.time.LocalTime
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping(CONTEST_PATH)
class DeveloperContestController(
    loginData: LoginData,

    private val contestService: ContestService
) : AbstractWebUserController<Developer, DeveloperView, DeveloperService>(loginData) {

    override val mainPath = CONTEST_PATH

    override val mainPage = CONTEST_PAGE

    override fun Developer.toView(timeZoneId: String?) = TODO()

    @PostMapping("/create")
    fun contestPost(
        @ModelAttribute("contest") contestView: ContestCreationView,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        val contest = contestView.toEntity(webUser, timezone)

        contestService.validate(contest, redirectAttributes, "redirect:$CONTESTS_PATH")?.let { return it }

        if (contest.startDate > contest.endDate) {
            redirectAttributes.addPopupMessage("Дата начала не может быть позже даты окончания.")
            return "redirect:$CONTESTS_PATH"
        }

        if (contest.startDate == contest.endDate && contest.duration == LocalTime.of(0, 0)) {
            redirectAttributes.addPopupMessage("Длительность тура должна быть положительной.")
            return "redirect:$CONTESTS_PATH"
        }

        contestService.save(contest)

        redirectAttributes.addPopupMessage("Тур ${contest.name} успешно создан.")

        return "redirect:$CONTESTS_PATH"
    }

    @GetMapping("/{contestId}")
    fun contestGet(
        @PathVariable("contestId") contestId: Long,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (!webUser.checkContestExistence(contestId)) {
            redirectAttributes.addPopupMessage("Тур с ID $contestId не найден.")
            return "redirect:$CONTESTS_PATH"
        }

        val contest = contestService.find(contestId) ?: run {
            redirectAttributes.addPopupMessage("Тур с ID $contestId не найден.")
            return "redirect:$CONTESTS_PATH"
        }

        val contestView = contest.toView(timezone)
        model.addAttribute(CONTEST_ATTR, contestView)

        return CONTEST_PAGE
    }

    @PostMapping("/update/{contestId}")
    fun contestUpdate(
        @PathVariable("contestId") contestId: Long,
        @ModelAttribute("contest") contestView: ContestView,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (!webUser.checkContestExistence(contestId)) {
            redirectAttributes.addPopupMessage("Тур с ID $contestId не найден.")
            return "redirect:$CONTESTS_PATH"
        }

        val contest = contestView.toEntity(timezone)
        contest.developer = webUser
        val groups = contestService.find(contestId)?.groups ?: mutableSetOf()
        contest.groups.addAll(groups)

        contestService.validate(contest, redirectAttributes, "redirect:$CONTEST_PATH/$contestId")?.let { return it }

        val updatedContest = contestService.save(contest)

        model.addAttribute(CONTEST_ATTR, updatedContest.toView(timezone))
        redirectAttributes.addPopupMessage("Данные успешно изменены.")

        return "redirect:$CONTEST_PATH/$contestId"
    }

    @PostMapping("/switchVisibility/{contestId}")
    fun contestSwitchVisibility(
        @PathVariable("contestId") contestId: Long,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (!webUser.checkContestExistence(contestId)) {
            redirectAttributes.addPopupMessage("Тур с ID $contestId не найден.")
            return "redirect:$CONTESTS_PATH"
        }

        val contest = contestService.find(contestId) ?: run {
            redirectAttributes.addPopupMessage("Тур с ID $contestId не найден.")
            return "redirect:$CONTESTS_PATH"
        }

        contest.switchVisibility()

        if (!contest.isPublic()) {
            contest.groups.clear()
        }

        contestService.save(contest)

        redirectAttributes.addPopupMessage("Видимость Тура '${contest.name}' изменена с '${contest.visibility.opposite()}' на '${contest.visibility}'.")

        return "redirect:$CONTESTS_PATH"
    }

    companion object {

        const val CONTEST_ATTR = "contest"

        const val CONTEST_PATH = "$CONTESTS_PATH/contest"
        const val CONTEST_PAGE = "$DEVELOPER_PAGE/contest"


        fun Developer.checkContestExistence(contestId: Long?) = contests.any { it.id == contestId }

        fun ContestService.validate(contest: Contest, redirectAttributes: RedirectAttributes, redirect: String): String? {
            if (!validateName(contest)) {
                redirectAttributes.addPopupMessage("Название Тура не должно содержать Код-доступа.")
                return redirect
            }

            if (!validateAdditionalInfo(contest)) {
                redirectAttributes.addPopupMessage("Дополнительная информация не должна содержать Код-доступа.")
                return redirect
            }

            return null
        }
    }
}