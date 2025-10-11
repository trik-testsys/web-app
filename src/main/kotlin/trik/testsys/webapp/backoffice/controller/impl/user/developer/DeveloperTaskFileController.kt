package trik.testsys.webapp.backoffice.controller.impl.user.developer

import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
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
import trik.testsys.webapp.backoffice.data.entity.impl.Solution
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile
import trik.testsys.webapp.backoffice.data.entity.impl.TaskFile.TaskFileType.Companion.extension
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ConditionFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.ExerciseFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.PolygonFile
import trik.testsys.webapp.backoffice.data.entity.impl.taskFile.SolutionFile
import trik.testsys.webapp.backoffice.data.enums.FileType
import trik.testsys.webapp.backoffice.data.service.TaskFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.ConditionFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.ExerciseFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.PolygonFileService
import trik.testsys.webapp.backoffice.data.service.impl.taskFile.SolutionFileService
import trik.testsys.webapp.backoffice.service.FileManager
import trik.testsys.webapp.backoffice.utils.addMessage
import java.nio.charset.StandardCharsets
import java.time.Instant

@Controller
@RequestMapping("/user/developer")
class DeveloperTaskFileController(
    private val taskFileService: TaskFileService,
    private val fileManager: FileManager,

    private val conditionFileService: ConditionFileService,
    private val exerciseFileService: ExerciseFileService,
    private val polygonFileService: PolygonFileService,
    private val solutionFileService: SolutionFileService
) : AbstractUserController() {

    @GetMapping("/task-files")
    fun taskFilesPage(model: Model, session: HttpSession, redirectAttributes: RedirectAttributes): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val developerId = developer.id!!

        val conditionFiles = conditionFileService.findByDeveloper(developerId)
        val exerciseFiles = exerciseFileService.findByDeveloper(developerId)
        val polygonFiles = polygonFileService.findByDeveloper(developerId)
        val solutionFiles = solutionFileService.findByDeveloper(developerId)

        setupModel(model, session, developer)
        model.addAttribute("polygonFiles", polygonFiles)
        model.addAttribute("exerciseFiles", exerciseFiles)
        model.addAttribute("solutionFiles", solutionFiles)
        model.addAttribute("conditionFiles", conditionFiles)

        return "developer/task-files"
    }

    @GetMapping("/task-files/condition")
    fun conditionFileCreateForm(
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

        setupModel(model, session, developer)

        return "developer/condition-create"
    }

    @GetMapping("/task-files/exercise")
    fun exerciseFileCreateForm(
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

        setupModel(model, session, developer)

        return "developer/exercise-create"
    }

    @GetMapping("/task-files/polygon")
    fun polygonFileCreateForm(
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

        setupModel(model, session, developer)

        return "developer/polygon-create"
    }

    @GetMapping("/task-files/solution")
    fun solutionFileCreateForm(
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

        setupModel(model, session, developer)

        return "developer/solution-create"
    }

    @PostMapping("/task-files/condition/create")
    fun conditionFileCreate(
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        @RequestParam type: FileType,
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

        if (!ConditionFile.allowedTypes.contains(type)) {
            redirectAttributes.addMessage("Некорректный тип файла.")
            return "redirect:/user/developer/task-files"
        }

        val conditionFile = ConditionFile().also {
            it.name = trimmedName
            it.data.originalFileNameByVersion[it.fileVersion] = file.originalFilename?.substringBeforeLast(".") ?: trimmedName
            it.developerId = developer.id
            it.info = info?.takeIf { s -> s.isNotBlank() }
            it.type = type
        }

        val saved = fileManager.saveConditionFile(conditionFile, file) ?: run {
            conditionFileService.delete(conditionFile)
            redirectAttributes.addMessage("Не удалось сохранить файл.")
            return "redirect:/user/developer/task-files"
        }

        redirectAttributes.addMessage("Файл создан (id=${saved.id}).")
        return "redirect:/user/developer/task-files"
    }

    @PostMapping("/task-files/exercise/create")
    fun exerciseFileCreate(
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        @RequestParam type: FileType,
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

        if (!ExerciseFile.allowedTypes.contains(type)) {
            redirectAttributes.addMessage("Некорректный тип файла.")
            return "redirect:/user/developer/task-files"
        }

        val exerciseFile = ExerciseFile().also {
            it.name = trimmedName
            it.data.originalFileNameByVersion[it.fileVersion] = file.originalFilename?.substringBeforeLast(".") ?: trimmedName
            it.developerId = developer.id
            it.info = info?.takeIf { s -> s.isNotBlank() }
            it.type = type
        }

        val saved = fileManager.saveExerciseFile(exerciseFile, file) ?: run {
            exerciseFileService.delete(exerciseFile)
            redirectAttributes.addMessage("Не удалось сохранить файл.")
            return "redirect:/user/developer/task-files"
        }

        redirectAttributes.addMessage("Файл создан (id=${saved.id}).")
        return "redirect:/user/developer/task-files"
    }

    @PostMapping("/task-files/polygon/create")
    fun polygonFileCreate(
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        @RequestParam type: FileType,
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

        if (!PolygonFile.allowedTypes.contains(type)) {
            redirectAttributes.addMessage("Некорректный тип файла.")
            return "redirect:/user/developer/task-files"
        }

        val polygonFile = PolygonFile().also {
            it.name = trimmedName
            it.data.originalFileNameByVersion[it.fileVersion] = file.originalFilename?.substringBeforeLast(".") ?: trimmedName
            it.developerId = developer.id
            it.info = info?.takeIf { s -> s.isNotBlank() }
            it.type = type
        }

        val saved = fileManager.savePolygonFile(polygonFile, file) ?: run {
            polygonFileService.delete(polygonFile)
            redirectAttributes.addMessage("Не удалось сохранить файл.")
            return "redirect:/user/developer/task-files"
        }

        redirectAttributes.addMessage("Файл создан (id=${saved.id}).")
        return "redirect:/user/developer/task-files"
    }

    @PostMapping("/task-files/solution/create")
    fun solutionFileCreate(
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        @RequestParam type: FileType,
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

        if (!SolutionFile.allowedTypes.contains(type)) {
            redirectAttributes.addMessage("Некорректный тип файла.")
            return "redirect:/user/developer/task-files"
        }

        val solutionFile = SolutionFile().also {
            it.name = trimmedName
            it.data.originalFileNameByVersion[it.fileVersion] = file.originalFilename?.substringBeforeLast(".") ?: trimmedName
            it.developerId = developer.id
            it.info = info?.takeIf { s -> s.isNotBlank() }
            it.type = type
            it.solutionType = type.toSolutionType()
        }

        val saved = fileManager.saveSolutionFile(solutionFile, file) ?: run {
            solutionFileService.delete(solutionFile)
            redirectAttributes.addMessage("Не удалось сохранить файл.")
            return "redirect:/user/developer/task-files"
        }

        redirectAttributes.addMessage("Файл создан (id=${saved.id}).")
        return "redirect:/user/developer/task-files"
    }

    private fun FileType.toSolutionType() = when (this) {
        FileType.PYTHON -> Solution.SolutionType.PYTHON
        FileType.QRS -> Solution.SolutionType.QRS
        FileType.JAVASCRIPT -> Solution.SolutionType.JAVA_SCRIPT
        else -> error("NOT ALLOWED FILE TYPE")
    }

    @GetMapping("/task-files/condition/{id}")
    fun viewConditionFile(
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

        val conditionFile = conditionFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }

        if (conditionFile.developerId != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этому Файлу.")
            return "redirect:/user/developer/task-files"
        }
        if (conditionFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён.")
            return "redirect:/user/developer/task-files"
        }

        val versions = fileManager.listFileVersions(conditionFile)

        setupModel(model, session, developer)
        model.addAttribute("taskFile", conditionFile)
        model.addAttribute("versions", versions)

        return "developer/condition-file"
    }

    @GetMapping("/task-files/exercise/{id}")
    fun viewExerciseFile(
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

        val exerciseFile = exerciseFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }

        if (exerciseFile.developerId != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этому Файлу.")
            return "redirect:/user/developer/task-files"
        }
        if (exerciseFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён.")
            return "redirect:/user/developer/task-files"
        }

        val versions = fileManager.listFileVersions(exerciseFile)

        setupModel(model, session, developer)
        model.addAttribute("taskFile", exerciseFile)
        model.addAttribute("versions", versions)

        return "developer/exercise-file"
    }

    @GetMapping("/task-files/polygon/{id}")
    fun viewPolygonFile(
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

        val polygonFile = polygonFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }

        if (polygonFile.developerId != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этому Файлу.")
            return "redirect:/user/developer/task-files"
        }
        if (polygonFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён.")
            return "redirect:/user/developer/task-files"
        }

        val versions = fileManager.listFileVersions(polygonFile)

        setupModel(model, session, developer)
        model.addAttribute("taskFile", polygonFile)
        model.addAttribute("versions", versions)

        return "developer/polygon-file"
    }

    @GetMapping("/task-files/solution/{id}")
    fun viewSolutionFile(
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

        val solutionFile = solutionFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }

        if (solutionFile.developerId != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этому Файлу.")
            return "redirect:/user/developer/task-files"
        }
        if (solutionFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён.")
            return "redirect:/user/developer/task-files"
        }

        val versions = fileManager.listFileVersions(solutionFile)

        setupModel(model, session, developer)
        model.addAttribute("taskFile", solutionFile)
        model.addAttribute("versions", versions)

        return "developer/solution-file"
    }

    @PostMapping("/task-files/condition/{id}/upload")
    fun updateConditionFile(
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

        val conditionFile = conditionFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (conditionFile.developerId != developer.id) {
            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
            return "redirect:/user/developer/task-files/condition/$id"
        }
        if (conditionFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён и недоступен для обновления.")
            return "redirect:/user/developer/task-files"
        }

        conditionFile.fileVersion++
        conditionFile.data.originalFileNameByVersion[conditionFile.fileVersion] = file.originalFilename?.substringBeforeLast('.')
            ?: conditionFile.name!!

        val saved = fileManager.saveConditionFile(conditionFile, file) ?: run {
            conditionFile.fileVersion--
            redirectAttributes.addMessage("Не удалось сохранить файл.")
            return "redirect:/user/developer/task-files/condition/$id"
        }

        conditionFileService.save(saved)
        redirectAttributes.addMessage("Файл обновлён.")
        return "redirect:/user/developer/task-files/condition/$id"
    }

    @PostMapping("/task-files/exercise/{id}/upload")
    fun updateExerciseFile(
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

        val exerciseFile = exerciseFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (exerciseFile.developerId != developer.id) {
            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
            return "redirect:/user/developer/task-files/exercise/$id"
        }
        if (exerciseFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён и недоступен для обновления.")
            return "redirect:/user/developer/task-files"
        }

        exerciseFile.fileVersion++
        exerciseFile.data.originalFileNameByVersion[exerciseFile.fileVersion] = file.originalFilename?.substringBeforeLast('.')
            ?: exerciseFile.name!!

        val saved = fileManager.saveExerciseFile(exerciseFile, file) ?: run {
            exerciseFile.fileVersion--
            redirectAttributes.addMessage("Не удалось сохранить файл.")
            return "redirect:/user/developer/task-files/exercise/$id"
        }

        exerciseFileService.save(saved)
        redirectAttributes.addMessage("Файл обновлён.")
        return "redirect:/user/developer/task-files/exercise/$id"
    }

    @PostMapping("/task-files/polygon/{id}/upload")
    fun updatePolygonFile(
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

        val polygonFile = polygonFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (polygonFile.developerId != developer.id) {
            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
            return "redirect:/user/developer/task-files/polygon/$id"
        }
        if (polygonFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён и недоступен для обновления.")
            return "redirect:/user/developer/task-files"
        }

        polygonFile.fileVersion++
        polygonFile.data.originalFileNameByVersion[polygonFile.fileVersion] = file.originalFilename?.substringBeforeLast('.')
            ?: polygonFile.name!!

        val saved = fileManager.savePolygonFile(polygonFile, file) ?: run {
            polygonFile.fileVersion--
            redirectAttributes.addMessage("Не удалось сохранить файл.")
            return "redirect:/user/developer/task-files/polygon/$id"
        }

        polygonFileService.save(saved)
        redirectAttributes.addMessage("Файл обновлён.")
        return "redirect:/user/developer/task-files/polygon/$id"
    }

    @PostMapping("/task-files/solution/{id}/upload")
    fun updateSolutionFile(
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

        val solutionFile = solutionFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (solutionFile.developerId != developer.id) {
            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
            return "redirect:/user/developer/task-files/solution/$id"
        }
        if (solutionFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён и недоступен для обновления.")
            return "redirect:/user/developer/task-files"
        }

        solutionFile.fileVersion++
        solutionFile.data.originalFileNameByVersion[solutionFile.fileVersion] = file.originalFilename?.substringBeforeLast('.')
            ?: solutionFile.name!!

        val saved = fileManager.saveSolutionFile(solutionFile, file) ?: run {
            solutionFile.fileVersion--
            redirectAttributes.addMessage("Не удалось сохранить файл.")
            return "redirect:/user/developer/task-files/solution/$id"
        }

        solutionFileService.save(saved)
        redirectAttributes.addMessage("Файл обновлён.")
        return "redirect:/user/developer/task-files/solution/$id"
    }

    @PostMapping("/task-files/condition/{id}/update")
    fun updateConditionFileMeta(
        @PathVariable id: Long,
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val conditionFile = conditionFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (conditionFile.developerId != developer.id) {
            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
            return "redirect:/user/developer/task-files/condition/$id"
        }
        if (conditionFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён и недоступен для обновления.")
            return "redirect:/user/developer/task-files"
        }

        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            redirectAttributes.addMessage("Название не может быть пустым.")
            return "redirect:/user/developer/task-files/condition/$id"
        }

        conditionFile.name = trimmedName
        conditionFile.info = info?.takeIf { it.isNotBlank() }
        conditionFileService.save(conditionFile)
        redirectAttributes.addMessage("Данные Файла обновлены.")
        return "redirect:/user/developer/task-files/condition/$id"
    }

    @PostMapping("/task-files/exercise/{id}/update")
    fun updateExerciseFileMeta(
        @PathVariable id: Long,
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val exerciseFile = exerciseFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (exerciseFile.developerId != developer.id) {
            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
            return "redirect:/user/developer/task-files/exercise/$id"
        }
        if (exerciseFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён и недоступен для обновления.")
            return "redirect:/user/developer/task-files"
        }

        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            redirectAttributes.addMessage("Название не может быть пустым.")
            return "redirect:/user/developer/task-files/exercise/$id"
        }

        exerciseFile.name = trimmedName
        exerciseFile.info = info?.takeIf { it.isNotBlank() }
        exerciseFileService.save(exerciseFile)
        redirectAttributes.addMessage("Данные Файла обновлены.")
        return "redirect:/user/developer/task-files/exercise/$id"
    }

    @PostMapping("/task-files/polygon/{id}/update")
    fun updatePolygonFileMeta(
        @PathVariable id: Long,
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val polygonFile = polygonFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (polygonFile.developerId != developer.id) {
            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
            return "redirect:/user/developer/task-files/polygon/$id"
        }
        if (polygonFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён и недоступен для обновления.")
            return "redirect:/user/developer/task-files"
        }

        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            redirectAttributes.addMessage("Название не может быть пустым.")
            return "redirect:/user/developer/task-files/polygon/$id"
        }

        polygonFile.name = trimmedName
        polygonFile.info = info?.takeIf { it.isNotBlank() }
        polygonFileService.save(polygonFile)
        redirectAttributes.addMessage("Данные Файла обновлены.")
        return "redirect:/user/developer/task-files/polygon/$id"
    }

    @PostMapping("/task-files/solution/{id}/update")
    fun updateSolutionFileMeta(
        @PathVariable id: Long,
        @RequestParam name: String,
        @RequestParam(required = false) info: String?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val solutionFile = solutionFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (solutionFile.developerId != developer.id) {
            redirectAttributes.addMessage("Редактирование доступно только владельцу.")
            return "redirect:/user/developer/task-files/solution/$id"
        }
        if (solutionFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён и недоступен для обновления.")
            return "redirect:/user/developer/task-files"
        }

        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            redirectAttributes.addMessage("Название не может быть пустым.")
            return "redirect:/user/developer/task-files/solution/$id"
        }

        solutionFile.name = trimmedName
        solutionFile.info = info?.takeIf { it.isNotBlank() }
        solutionFileService.save(solutionFile)
        redirectAttributes.addMessage("Данные Файла обновлены.")
        return "redirect:/user/developer/task-files/solution/$id"
    }

    @PostMapping("/task-files/condition/{id}/delete")
    fun deleteConditionFile(
        @PathVariable id: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val conditionFile = conditionFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (conditionFile.developerId != developer.id) {
            redirectAttributes.addMessage("Удаление доступно только владельцу.")
            return "redirect:/user/developer/task-files/condition/$id"
        }

        if (conditionFile.taskIds.isNotEmpty()) {
            redirectAttributes.addMessage("Нельзя удалить Файл, прикреплённый к Задаче.")
            return "redirect:/user/developer/task-files/condition/$id"
        }

        conditionFile.isRemoved = true
        conditionFileService.save(conditionFile)

        logger.info("ConditionFile(id=${conditionFile.id}) was marked as removed.")

        redirectAttributes.addMessage("Файл удален.")
        return "redirect:/user/developer/task-files"
    }

    @PostMapping("/task-files/exercise/{id}/delete")
    fun deleteExerciseFile(
        @PathVariable id: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val exerciseFile = exerciseFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (exerciseFile.developerId != developer.id) {
            redirectAttributes.addMessage("Удаление доступно только владельцу.")
            return "redirect:/user/developer/task-files/exercise/$id"
        }

        if (exerciseFile.taskIds.isNotEmpty()) {
            redirectAttributes.addMessage("Нельзя удалить Файл, прикреплённый к Задаче.")
            return "redirect:/user/developer/task-files/exercise/$id"
        }

        exerciseFile.isRemoved = true
        exerciseFileService.save(exerciseFile)

        logger.info("ExerciseFile(id=${exerciseFile.id}) was marked as removed.")

        redirectAttributes.addMessage("Файл удален.")
        return "redirect:/user/developer/task-files"
    }

    @PostMapping("/task-files/polygon/{id}/delete")
    fun deletePolygonFile(
        @PathVariable id: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val polygonFile = polygonFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (polygonFile.developerId != developer.id) {
            redirectAttributes.addMessage("Удаление доступно только владельцу.")
            return "redirect:/user/developer/task-files/polygon/$id"
        }

        if (polygonFile.taskIds.isNotEmpty()) {
            redirectAttributes.addMessage("Нельзя удалить Файл, прикреплённый к Задаче.")
            return "redirect:/user/developer/task-files/polygon/$id"
        }

        polygonFile.isRemoved = true
        polygonFileService.save(polygonFile)

        logger.info("PolygonFile(id=${polygonFile.id}) was marked as removed.")

        redirectAttributes.addMessage("Файл удален.")
        return "redirect:/user/developer/task-files"
    }

    @PostMapping("/task-files/solution/{id}/delete")
    fun deleteSolutionFile(
        @PathVariable id: Long,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val accessToken = getAccessToken(session, redirectAttributes) ?: return "redirect:/login"
        val developer = getUser(accessToken, redirectAttributes) ?: return "redirect:/login"

        if (!developer.privileges.contains(User.Privilege.DEVELOPER)) {
            redirectAttributes.addMessage("Недостаточно прав.")
            return "redirect:/user"
        }

        val solutionFile = solutionFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (solutionFile.developerId != developer.id) {
            redirectAttributes.addMessage("Удаление доступно только владельцу.")
            return "redirect:/user/developer/task-files/solution/$id"
        }

        if (solutionFile.taskIds.isNotEmpty()) {
            redirectAttributes.addMessage("Нельзя удалить Файл, прикреплённый к Задаче.")
            return "redirect:/user/developer/task-files/solution/$id"
        }

        solutionFile.isRemoved = true
        solutionFileService.save(solutionFile)

        logger.info("SolutionFile(id=${solutionFile.id}) was marked as removed.")

        redirectAttributes.addMessage("Файл удален.")
        return "redirect:/user/developer/task-files"
    }

    @GetMapping("/task-files/condition/{id}/download/{version}")
    fun downloadConditionFileVersion(
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

        val conditionFile = conditionFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (conditionFile.developerId != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этому Файлу.")
            return "redirect:/user/developer/task-files"
        }
        if (conditionFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён.")
            return "redirect:/user/developer/task-files"
        }

        val file = fileManager.getConditionFile(conditionFile, version) ?: run {
            redirectAttributes.addMessage("Версия не найдена.")
            return "redirect:/user/developer/task-files/condition/$id"
        }

        val bytes = file.readBytes()
        val name = conditionFile.data.originalFileNameByVersion[version] ?: conditionFile.name
        val filename = "${name}${conditionFile.type?.extension}"
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

    @GetMapping("/task-files/exercise/{id}/download/{version}")
    fun downloadExerciseFileVersion(
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

        val exerciseFile = exerciseFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (exerciseFile.developerId != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этому Файлу.")
            return "redirect:/user/developer/task-files"
        }
        if (exerciseFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён.")
            return "redirect:/user/developer/task-files"
        }

        val file = fileManager.getExerciseFile(exerciseFile, version) ?: run {
            redirectAttributes.addMessage("Версия не найдена.")
            return "redirect:/user/developer/task-files/exercise/$id"
        }

        val bytes = file.readBytes()
        val name = exerciseFile.data.originalFileNameByVersion[version] ?: exerciseFile.name
        val filename = "${name}${exerciseFile.type?.extension}"
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

    @GetMapping("/task-files/polygon/{id}/download/{version}")
    fun downloadPolygonFileVersion(
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

        val polygonFile = polygonFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (polygonFile.developerId != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этому Файлу.")
            return "redirect:/user/developer/task-files"
        }
        if (polygonFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён.")
            return "redirect:/user/developer/task-files"
        }

        val file = fileManager.getPolygonFile(polygonFile, version) ?: run {
            redirectAttributes.addMessage("Версия не найдена.")
            return "redirect:/user/developer/task-files/polygon/$id"
        }

        val bytes = file.readBytes()
        val name = polygonFile.data.originalFileNameByVersion[version] ?: polygonFile.name
        val filename = "${name}${polygonFile.type?.extension}"
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

    @GetMapping("/task-files/solution/{id}/download/{version}")
    fun downloadSolutionFileVersion(
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

        val solutionFile = solutionFileService.findById(id) ?: run {
            redirectAttributes.addMessage("Файл не найден.")
            return "redirect:/user/developer/task-files"
        }
        if (solutionFile.developerId != developer.id) {
            redirectAttributes.addMessage("У вас нет доступа к этому Файлу.")
            return "redirect:/user/developer/task-files"
        }
        if (solutionFile.isRemoved) {
            redirectAttributes.addMessage("Файл удалён.")
            return "redirect:/user/developer/task-files"
        }

        val file = fileManager.getSolutionFile(solutionFile, version) ?: run {
            redirectAttributes.addMessage("Версия не найдена.")
            return "redirect:/user/developer/task-files/solution/$id"
        }

        val bytes = file.readBytes()
        val name = solutionFile.data.originalFileNameByVersion[version] ?: solutionFile.name
        val filename = "${name}${solutionFile.type?.extension}"
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

    companion object {

        private val logger = LoggerFactory.getLogger(DeveloperTaskFileController::class.java)
    }
}
