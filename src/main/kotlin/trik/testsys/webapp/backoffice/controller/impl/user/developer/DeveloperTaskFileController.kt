package trik.testsys.webapp.backoffice.controller.impl.user.developer

import jakarta.servlet.http.HttpSession
import org.springframework.http.MediaType
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import trik.testsys.webapp.backoffice.controller.AbstractUserController
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile.TaskFileType.Companion.extension
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.TaskFileService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.utils.addMessage
import java.nio.charset.StandardCharsets
import java.time.Instant

@Controller
@RequestMapping("/user/developer")
class DeveloperTaskFileController(
    private val taskFileService: TaskFileService,
    private val fileManager: FileManager,
) : AbstractUserController() {

    @GetMapping("/task-files")
    fun taskFilesPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val allTaskFiles = taskFileService.findByDeveloper(developer).sortedBy { it.id }

        fun toListItem(tf: TaskFile) = TaskFileListItem(
            id = tf.id!!,
            name = tf.name,
            info = tf.info,
            createdAt = tf.createdAt,
            localizedType = when (tf.type) {
                TaskFile.TaskFileType.POLYGON -> "Полигон"
                TaskFile.TaskFileType.EXERCISE -> "Упражнение"
                TaskFile.TaskFileType.SOLUTION -> "Эталонное Решение"
                TaskFile.TaskFileType.CONDITION -> "Условие"
                else -> tf.type?.name ?: "—"
            }
        )

        val polygonFiles = allTaskFiles.filter { it.type == TaskFile.TaskFileType.POLYGON }.map(::toListItem)
        val exerciseFiles = allTaskFiles.filter { it.type == TaskFile.TaskFileType.EXERCISE }.map(::toListItem)
        val solutionFiles = allTaskFiles.filter { it.type == TaskFile.TaskFileType.SOLUTION }.map(::toListItem)
        val conditionFiles = allTaskFiles.filter { it.type == TaskFile.TaskFileType.CONDITION }.map(::toListItem)

        setupModel(model, session, developer)
        model.addAttribute("polygonFiles", polygonFiles)
        model.addAttribute("exerciseFiles", exerciseFiles)
        model.addAttribute("solutionFiles", solutionFiles)
        model.addAttribute("conditionFiles", conditionFiles)

        return "developer/task-files"
    }

    @GetMapping("/task-files/create")
    fun taskFileCreateForm(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        setupModel(model, session, developer)

        return "developer/task-file-create"
    }

    @PostMapping("/task-files/create")
    fun createTaskFile(
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        @RequestParam type: String,
        @RequestParam("file") file: MultipartFile,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            redirectAttributes.addMessage("Название не может быть пустым.")
            return "redirect:/user/developer/task-files"
        }

        val tfType = try {
            TaskFile.TaskFileType.valueOf(type)
        } catch (e: Exception) {
            redirectAttributes.addMessage("Некорректный тип файла.")
            return "redirect:/user/developer/task-files"
        }

        val taskFile = TaskFile().also {
            it.name = trimmedName
            it.data.originalFileNameByVersion[it.fileVersion] = file.originalFilename?.substringBeforeLast('.')
                ?: trimmedName
            it.developer = developer
            it.info = info?.takeIf { s -> s.isNotBlank() }
            it.type = tfType
        }

        val saved = fileManager.saveTaskFile(taskFile, file)
        if (!saved) {
            taskFileService.delete(taskFile)
            redirectAttributes.addMessage("Не удалось сохранить файл на диск.")
            return "redirect:/user/developer/task-files"
        }

        redirectAttributes.addMessage("Файл создан (id=${taskFile.id}).")
        return "redirect:/user/developer/task-files"
    }

    @GetMapping("/task-files/{id}")
    fun viewTaskFile(
        @PathVariable id: Long,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val tf = taskFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (tf.developer?.id != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этому Файлу.")
            return "redirect:/user/developer/task-files"
        }

        val localizedType = when (tf.type) {
            TaskFile.TaskFileType.POLYGON -> "Полигон"
            TaskFile.TaskFileType.EXERCISE -> "Упражнение"
            TaskFile.TaskFileType.SOLUTION -> "Эталонное Решение"
            TaskFile.TaskFileType.CONDITION -> "Условие"
            else -> tf.type?.name ?: "—"
        }

        val versions = fileManager.listTaskFileVersions(tf)

        setupModel(model, session, developer)
        model.addAttribute("taskFile", tf)
        model.addAttribute("localizedType", localizedType)
        model.addAttribute("versions", versions)

        return "developer/task-file"
    }

    @PostMapping("/task-files/{id}/upload")
    fun updateTaskFile(
        @PathVariable id: Long,
        @RequestParam("file") file: MultipartFile,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val tf = taskFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (tf.developer?.id != developer.id) {
            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
            return "redirect:/user/developer/task-files/$id"
        }

        tf.fileVersion = (tf.fileVersion + 1)
        tf.data.originalFileNameByVersion[tf.fileVersion] = file.originalFilename?.substringBeforeLast('.')
            ?: tf.name!!

        val saved = fileManager.saveTaskFile(tf, file)
        if (!saved) {
            tf.fileVersion = (tf.fileVersion - 1)
            redirectAttributes.addMessage("Не удалось сохранить файл.")
            return "redirect:/user/developer/task-files/$id"
        }

        taskFileService.save(tf)
        redirectAttributes.addMessage("Файл обновлён.")
        return "redirect:/user/developer/task-files/$id"
    }

    @GetMapping("/task-files/{id}/download/{version}")
    fun downloadTaskFileVersion(
        @PathVariable id: Long,
        @PathVariable version: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): Any {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val tf = taskFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (tf.developer?.id != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этому Файлу.")
            return "redirect:/user/developer/task-files"
        }

        val file = fileManager.getTaskFileVersion(tf, version) ?: run {
            redirectAttributes.addMessage("Версия не найдена.")
            return "redirect:/user/developer/task-files/$id"
        }

        val bytes = file.readBytes()
        val name = tf.data.originalFileNameByVersion[version] ?: tf.name
        val filename = "${name}${tf.type?.extension()}"
        val disposition = ContentDisposition
            .attachment()
            .filename(filename, StandardCharsets.UTF_8)
            .build()
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header(HttpHeaders.CONTENT_LENGTH, bytes.size.toString())
            .body(bytes)
    }

    private data class TaskFileListItem(
        val id: Long,
        val name: String?,
        val info: String?,
        val createdAt: Instant?,
        val localizedType: String,
    )
}
