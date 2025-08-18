//package trik.testsys.webapp.backoffice.controller.impl.user.developer
//
//import org.springframework.http.ResponseEntity
//import org.springframework.stereotype.Controller
//import org.springframework.ui.Model
//import org.springframework.web.bind.annotation.*
//import org.springframework.web.multipart.MultipartFile
//import org.springframework.web.servlet.mvc.support.RedirectAttributes
//import trik.testsys.backoffice.controller.impl.user.developer.DeveloperMainController.Companion.DEVELOPER_PAGE
//import trik.testsys.backoffice.controller.impl.user.developer.DeveloperMainController.Companion.DEVELOPER_PATH
//import trik.testsys.backoffice.controller.impl.user.developer.DeveloperTaskFilesController.Companion.TASK_FILES_PATH
//import trik.testsys.backoffice.controller.user.AbstractWebUserController
//import trik.testsys.backoffice.controller.user.AbstractWebUserMainController.Companion.LOGIN_PATH
//import trik.testsys.backoffice.entity.impl.TaskFile
//import trik.testsys.backoffice.entity.impl.TaskFile.TaskFileType.Companion.localized
//import trik.testsys.backoffice.entity.impl.TaskFileAudit
//import trik.testsys.backoffice.entity.user.impl.Developer
//import trik.testsys.backoffice.service.FileManager
//import trik.testsys.backoffice.service.entity.impl.TaskFileAuditService
//import trik.testsys.backoffice.service.entity.impl.TaskFileService
//import trik.testsys.backoffice.service.entity.user.impl.DeveloperService
//import trik.testsys.backoffice.service.security.login.impl.LoginData
//import trik.testsys.backoffice.util.addPopupMessage
//import trik.testsys.backoffice.view.impl.DeveloperView
//import trik.testsys.backoffice.view.impl.TaskFileAuditCreationView
//import trik.testsys.backoffice.view.impl.TaskFileCreationView
//import trik.testsys.backoffice.view.impl.TaskFileView
//import java.util.*
//import javax.servlet.http.HttpServletRequest
//
//@Controller
//@RequestMapping(DeveloperTaskFileController.TASK_FILE_PATH)
//class DeveloperTaskFileController(
//    loginData: LoginData,
//
//    private val taskFileService: TaskFileService,
//    private val taskFileAuditService: TaskFileAuditService,
//    private val fileManager: FileManager
//) : AbstractWebUserController<Developer, DeveloperView, DeveloperService>(loginData) {
//
//    override val mainPage = TASK_FILE_PAGE
//
//    override val mainPath = TASK_FILE_PATH
//
//    override fun Developer.toView(timeZoneId: String?) = TODO("Not yet implemented")
//
//    @PostMapping("/create")
//    fun taskFilePost(
//        @ModelAttribute("taskFileView") taskFileView: TaskFileCreationView,
//        @RequestParam("file") file: MultipartFile,
//        timeZone: TimeZone,
//        request: HttpServletRequest,
//        redirectAttributes: RedirectAttributes
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"
//
//        val taskFile = taskFileView.toEntity(webUser)
//
//        taskFileService.validate(taskFile, redirectAttributes, "redirect:$TASK_FILES_PATH")?.let { return it }
//        taskFileService.save(taskFile)
//
//        val taskFileAudit = TaskFileAudit(taskFile)
//        taskFileAuditService.save(taskFileAudit)
//
//        val fileSavingResult = fileManager.saveTaskFile(taskFileAudit, file)
//        if (!fileSavingResult) {
//            taskFileAuditService.delete(taskFileAudit)
//            taskFileService.delete(taskFile)
//            redirectAttributes.addPopupMessage("Ошибка при сохранении файла.")
//            return "redirect:$TASK_FILES_PATH"
//        }
//
//        redirectAttributes.addPopupMessage("${taskFile.type.localized()} ${taskFile.name} успешно создан.")
//
//        val anchor = when (taskFile.type) {
//            TaskFile.TaskFileType.POLYGON -> ""
//            TaskFile.TaskFileType.EXERCISE -> "exercises"
//            TaskFile.TaskFileType.SOLUTION -> "solutions"
//            TaskFile.TaskFileType.CONDITION -> "conditions"
//        }
//
//        return "redirect:$TASK_FILES_PATH#$anchor"
//    }
//
//    @GetMapping("/{taskFileId}")
//    fun taskFileGet(
//        @PathVariable("taskFileId") taskFileId: Long,
//        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"
//
//        if (!webUser.checkTaskFileExistence(taskFileId)) {
//            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не найден.")
//            return "redirect:$TASK_FILES_PATH"
//        }
//
//        val taskFile = taskFileService.find(taskFileId) ?: run {
//            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не найден.")
//            return "redirect:$TASK_FILES_PATH"
//        }
//
//        val taskFileView = taskFile.toView(timezone)
//        model.addAttribute(TASK_FILE_ATTR, taskFileView)
//
//        model.addAttribute(TASK_FILE_AUDIT_ATTR, TaskFileAuditCreationView.empty())
//
//        return TASK_FILE_PAGE
//    }
//
//    @GetMapping("/download/{taskFileId}")
//    fun taskFileDownload(
//        @PathVariable("taskFileId") taskFileId: Long,
//        redirectAttributes: RedirectAttributes
//    ): Any {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"
//
//        if (!webUser.checkTaskFileExistence(taskFileId)) {
//            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не найден.")
//            return "redirect:$TASK_FILES_PATH"
//        }
//
//        val taskFile = taskFileService.find(taskFileId) ?: run {
//            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не найден.")
//            return "redirect:$TASK_FILES_PATH"
//        }
//
//        val file = fileManager.getTaskFile(taskFile) ?: run {
//            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не найден.")
//            return "redirect:$TASK_FILES_PATH"
//        }
//
//        val extension = fileManager.getTaskFileExtension(taskFile)
//        val responseEntity = ResponseEntity.ok()
//            .header("Content-Disposition", "attachment; filename=${taskFile.latestFileName}${extension}")
//            .body(file.readBytes())
//
//        return responseEntity
//    }
//
//    @PostMapping("/updateFile/{taskFileId}")
//    fun updateFile(
//        @PathVariable("taskFileId") taskFileId: Long,
//        @RequestParam("file") file: MultipartFile,
//        @ModelAttribute("taskFileAudit") taskFileAuditView: TaskFileAuditCreationView,
//        redirectAttributes: RedirectAttributes
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"
//
//        if (!webUser.checkTaskFileExistence(taskFileId)) {
//            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не найден.")
//            return "redirect:$DEVELOPER_PATH$TASK_FILES_PATH"
//        }
//
//        val taskFile = taskFileService.find(taskFileId) ?: run {
//            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не найден.")
//            return "redirect:$DEVELOPER_PATH$TASK_FILES_PATH"
//        }
//
//        if (taskFile.tasks.any { it.contests.any { contest -> contest.isPublic() } }) {
//            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не может быть изменен, так как он используется в публичных контестах.")
//            return "redirect:$TASK_FILE_PATH/$taskFileId"
//        }
//
//        val taskFileAudit = taskFileAuditView.toEntity(taskFile)
//        taskFileAuditService.save(taskFileAudit)
//
//        val fileSavingResult = fileManager.saveTaskFile(taskFileAudit, file)
//        if (!fileSavingResult) {
//            taskFileAuditService.delete(taskFileAudit)
//            redirectAttributes.addPopupMessage("Ошибка при сохранении файла.")
//            return "redirect:$TASK_FILE_PATH/$taskFileId"
//        }
//
//        redirectAttributes.addPopupMessage("Файл успешно обновлен.")
//
//        return "redirect:$TASK_FILE_PATH/$taskFileId"
//    }
//
//    @GetMapping("/downloadAudit/{taskFileAuditId}")
//    fun downloadAudit(
//        @PathVariable("taskFileAuditId") taskFileAuditId: Long,
//        redirectAttributes: RedirectAttributes
//    ): Any {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"
//
//        val taskFileAudit = taskFileAuditService.find(taskFileAuditId) ?: run {
//            redirectAttributes.addPopupMessage("Аудит с ID $taskFileAuditId не найден.")
//            return "redirect:$TASK_FILES_PATH"
//        }
//
//        val file = fileManager.getTaskFileAuditFile(taskFileAudit) ?: run {
//            redirectAttributes.addPopupMessage("Файл с ID $taskFileAuditId не найден.")
//            return "redirect:$TASK_FILES_PATH"
//        }
//
//        val extension = fileManager.getTaskFileExtension(taskFileAudit.taskFile)
//        val responseEntity = ResponseEntity.ok()
//            .header("Content-Disposition", "attachment; filename=${taskFileAudit.fileName}${extension}")
//            .body(file.readBytes())
//
//        return responseEntity
//    }
//
//    @PostMapping("/update/{taskFileId}")
//    fun taskFileUpdate(
//        @PathVariable("taskFileId") taskFileId: Long,
//        @ModelAttribute("taskFile") taskFileView: TaskFileView,
//        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"
//
//        if (!webUser.checkTaskFileExistence(taskFileId)) {
//            redirectAttributes.addPopupMessage("Файл с ID $taskFileId не найден.")
//            return "redirect:$DEVELOPER_PATH$TASK_FILES_PATH"
//        }
//
//        val taskFile = taskFileView.toEntity(timezone)
//        taskFile.developer = webUser
//
//        taskFileService.validate(taskFile, redirectAttributes, "redirect:$TASK_FILE_PATH/$taskFileId")?.let { return it }
//
//        val updatedTaskFile = taskFileService.save(taskFile)
//
//        model.addAttribute(TASK_FILE_ATTR, updatedTaskFile.toView(timezone))
//        redirectAttributes.addPopupMessage("Данные успешно изменены.")
//
//        return "redirect:$TASK_FILE_PATH/$taskFileId"
//    }
//
//    companion object {
//
//        const val TASK_FILE_PATH = "$TASK_FILES_PATH/taskFile"
//        const val TASK_FILE_PAGE = "$DEVELOPER_PAGE/taskFile"
//
//        const val TASK_FILE_ATTR = "taskFile"
//
//        const val TASK_FILE_AUDIT_ATTR = "taskFileAudit"
//
//        fun Developer.checkTaskFileExistence(taskFileId: Long?) = taskFiles.any { it.id == taskFileId }
//
//        fun TaskFileService.validate(taskFile: TaskFile, redirectAttributes: RedirectAttributes, redirect: String): String? {
//            if (!validateName(taskFile)) {
//                val taskTileTypeLocalized = taskFile.type.localized()
//                redirectAttributes.addPopupMessage("Название Файла с типом '$taskTileTypeLocalized' не должно содержать Код-доступа.")
//                return redirect
//            }
//
//            if (!validateAdditionalInfo(taskFile)) {
//                redirectAttributes.addPopupMessage("Дополнительная информация не должна содержать Код-доступа.")
//                return redirect
//            }
//
//            return null
//        }
//    }
//}