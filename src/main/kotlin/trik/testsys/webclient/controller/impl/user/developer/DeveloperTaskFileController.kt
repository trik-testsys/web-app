package trik.testsys.webclient.controller.impl.user.developer

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webclient.controller.impl.user.developer.DeveloperMainController.Companion.DEVELOPER_PAGE
import trik.testsys.webclient.controller.impl.user.developer.DeveloperMainController.Companion.DEVELOPER_PATH
import trik.testsys.webclient.controller.impl.user.developer.DeveloperTaskFilesController.Companion.TASK_FILES_PATH
import trik.testsys.webclient.controller.user.AbstractWebUserController
import trik.testsys.webclient.controller.user.AbstractWebUserMainController.Companion.LOGIN_PATH
import trik.testsys.webclient.entity.impl.TaskFile
import trik.testsys.webclient.entity.impl.TaskFile.TaskFileType.Companion.localized
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.service.FileManager
import trik.testsys.webclient.service.entity.impl.TaskFileService
import trik.testsys.webclient.service.entity.user.impl.DeveloperService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.addPopupMessage
import trik.testsys.webclient.view.impl.DeveloperView
import trik.testsys.webclient.view.impl.TaskFileCreationView
import trik.testsys.webclient.view.impl.TaskFileView
import trik.testsys.webclient.view.impl.TaskFileView.Companion.toView
import java.util.*
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping(DeveloperTaskFileController.TASK_FILE_PATH)
class DeveloperTaskFileController(
    loginData: LoginData,

    private val taskFileService: TaskFileService,
    private val fileManager: FileManager
) : AbstractWebUserController<Developer, DeveloperView, DeveloperService>(loginData) {

    override val mainPage = TASK_FILE_PAGE

    override val mainPath = TASK_FILE_PATH

    override fun Developer.toView(timeZoneId: String?) = TODO("Not yet implemented")

    @PostMapping("/create")
    fun taskFilePost(
        @ModelAttribute("taskFileView") taskFileView: TaskFileCreationView,
        @RequestParam("file") file: MultipartFile,
        timeZone: TimeZone,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        val taskFile = taskFileView.toEntity(webUser)

        taskFileService.validate(taskFile, redirectAttributes, "redirect:$TASK_FILES_PATH")?.let { return it }

        taskFileService.save(taskFile)

        val fileSavingResult = fileManager.saveTaskFile(taskFile, file)
        if (!fileSavingResult) {
            taskFileService.delete(taskFile)
            redirectAttributes.addPopupMessage("Ошибка при сохранении файла.")
            return "redirect:$TASK_FILES_PATH"
        }

        redirectAttributes.addPopupMessage("${taskFile.type.localized()} ${taskFile.name} успешно создан.")

        val anchor = when (taskFile.type) {
            TaskFile.TaskFileType.POLYGON -> ""
            TaskFile.TaskFileType.EXERCISE -> "exercises"
            TaskFile.TaskFileType.SOLUTION -> "solutions"
            TaskFile.TaskFileType.CONDITION -> "conditions"
        }

        return "redirect:$TASK_FILES_PATH#$anchor"
    }

    @GetMapping("/{taskFileId}")
    fun taskFileGet(
        @PathVariable("taskFileId") taskFileId: Long,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (!webUser.checkTaskFileExistence(taskFileId)) {
            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не найден.")
            return "redirect:$TASK_FILES_PATH"
        }

        val taskFile = taskFileService.find(taskFileId) ?: run {
            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не найден.")
            return "redirect:$TASK_FILES_PATH"
        }

        val taskFileView = taskFile.toView(timezone)
        model.addAttribute(TASK_FILE_ATTR, taskFileView)

        return TASK_FILE_PAGE
    }

    @PostMapping("/update/{taskFileId}")
    fun taskFileUpdate(
        @PathVariable("taskFileId") taskFileId: Long,
        @ModelAttribute("taskFile") taskFileView: TaskFileView,
        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"

        if (!webUser.checkTaskFileExistence(taskFileId)) {
            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не найден.")
            return "redirect:$DEVELOPER_PATH$TASK_FILES_PATH"
        }

        val taskFile = taskFileView.toEntity(timezone)
        taskFile.developer = webUser

        taskFileService.validate(taskFile, redirectAttributes, "redirect:$TASK_FILE_PATH/$taskFileId")?.let { return it }

        val updatedTaskFile = taskFileService.save(taskFile)

        model.addAttribute(TASK_FILE_ATTR, updatedTaskFile.toView(timezone))
        redirectAttributes.addPopupMessage("Данные успешно изменены.")

        return "redirect:$TASK_FILE_PATH/$taskFileId"
    }

    companion object {

        const val TASK_FILE_PATH = "$TASK_FILES_PATH/taskFile"
        const val TASK_FILE_PAGE = "$DEVELOPER_PAGE/taskFile"

        const val TASK_FILE_ATTR = "taskFile"

        fun Developer.checkTaskFileExistence(taskFileId: Long?) = taskFiles.any { it.id == taskFileId }

        fun TaskFileService.validate(taskFile: TaskFile, redirectAttributes: RedirectAttributes, redirect: String): String? {
            if (!validateName(taskFile)) {
                val taskTileTypeLocalized = taskFile.type.localized()
                redirectAttributes.addPopupMessage("Название Файла с типом '$taskTileTypeLocalized' не должно содержать Код-доступа.")
                return redirect
            }

            if (!validateAdditionalInfo(taskFile)) {
                redirectAttributes.addPopupMessage("Дополнительная информация не должна содержать Код-доступа.")
                return redirect
            }

            return null
        }
    }
}