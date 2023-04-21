package trik.testsys.webclient.controllers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

import trik.testsys.webclient.enums.WebUserStatuses
import trik.testsys.webclient.models.ResponseMessage
import trik.testsys.webclient.services.*

@RestController
@RequestMapping("/v1/testsys/admin")
class AdminController {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var adminService: AdminService

    @Autowired
    private lateinit var webUserService: WebUserService

    @Autowired
    private lateinit var groupService: GroupService

    @Autowired
    private lateinit var taskService: TaskService

    @Autowired
    private lateinit var studentService: StudentService

    @GetMapping
    fun getAccess(@RequestParam accessToken: String, model: Model): Any {
        logger.info("[${accessToken.padStart(80)}]: Client trying to access admin page.")

        val status = validateAdmin(accessToken)
        if (status == WebUserStatuses.ADMIN) {
            logger.info("[${accessToken.padStart(80)}]: Client is an admin.")
            val webUser = webUserService.getWebUserByAccessToken(accessToken)!!
            val admin = adminService.getAdminByWebUser(webUser)!!

            model.addAttribute("name", webUser.username)
            model.addAttribute("accessToken", accessToken)
            model.addAttribute("groups", admin.groups.sortedBy { it.id })
            return model
        }

        logger.info("[${accessToken.padStart(80)}]: Client is not an admin.")
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ResponseMessage(403, "You are not an admin!"))
    }

    @PostMapping("/group/create")
    fun createGroup(@RequestParam accessToken: String, @RequestParam name: String, model: Model): Any {
        logger.info("[${accessToken.padStart(80)}]: Client trying to create a group.")

        val status = validateAdmin(accessToken)
        if (status != WebUserStatuses.ADMIN) {
            logger.info("[${accessToken.padStart(80)}]: Client is not an admin.")
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseMessage(403, "You are not an admin!"))
        }

        logger.info("[${accessToken.padStart(80)}]: Client is an admin.")
        val webUser = webUserService.getWebUserByAccessToken(accessToken)!!
        val admin = adminService.getAdminByWebUser(webUser)!!

        model.addAttribute("accessToken", webUser.accessToken)

        val group = groupService.createGroup(admin, name)
        if (group != null) {
            logger.info("[${accessToken.padStart(80)}]: Group created.")

            model.addAttribute("isCreated", true)
            model.addAttribute("id", group.id!!)
            model.addAttribute("name", group.name)
            model.addAttribute("groupAccessToken", group.accessToken)

            return model
        }

        logger.info("[${accessToken.padStart(80)}]: Group already exists.")

        model.addAttribute("isCreated", false)
        model.addAttribute("message", "Группа с названием $name уже существует.")

        return model
    }

    @GetMapping("/group")
    fun accessToGroup(@RequestParam accessToken: String, @RequestParam groupAccessToken: String, model: Model): Any {
        logger.info("[${accessToken.padStart(80)}]: Client trying to access group.")

        val status = validateAdmin(accessToken)
        if (status != WebUserStatuses.ADMIN) {
            logger.info("[${accessToken.padStart(80)}]: Client is not an admin.")
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseMessage(403, "You are not an admin!"))
        }

        logger.info("[${accessToken.padStart(80)}]: Client is an admin.")
        val webUser = webUserService.getWebUserByAccessToken(accessToken)!!
        val admin = adminService.getAdminByWebUser(webUser)!!

        model.addAttribute("accessToken", webUser.accessToken)

        val group = groupService.getGroupByAccessToken(groupAccessToken)
        if (group == null) {
            logger.info("[${accessToken.padStart(80)}]: Group not found.")

            model.addAttribute("isFound", false)
            model.addAttribute("message", "Группа не найдена.")

            return model
        }

        if (group.admin != admin) {
            logger.info("[${accessToken.padStart(80)}]: Group not found.")

            model.addAttribute("isFound", false)
            model.addAttribute("message", "Group not found.")

            return model
        }

        logger.info("[${accessToken.padStart(80)}]: Group found.")

        model.addAttribute("isFound", true)
        model.addAttribute("id", group.id!!)
        model.addAttribute("name", group.name)
        model.addAttribute("groupAccessToken", group.accessToken)
        model.addAttribute("tasks", group.tasks.sortedBy { it.id })
        model.addAttribute("students", group.students.sortedBy { it.id })

        return model
    }

    @PostMapping("/task/create")
    fun createTask(
        @RequestParam accessToken: String,
        @RequestParam groupAccessToken: String,
        @RequestParam name: String,
        @RequestParam description: String,
        @RequestBody tests: List<MultipartFile>,
        model: Model
    ): Any {
        logger.info("[${accessToken.padStart(80)}]: Client trying to create a task.")

        val status = validateAdmin(accessToken)
        if (status != WebUserStatuses.ADMIN) {
            logger.info("[${accessToken.padStart(80)}]: Client is not an admin.")
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseMessage(403, "You are not an admin!"))
        }

        logger.info("[${accessToken.padStart(80)}]: Client is an admin.")
        val webUser = webUserService.getWebUserByAccessToken(accessToken)!!
        val admin = adminService.getAdminByWebUser(webUser)!!

        model.addAttribute("accessToken", webUser.accessToken)

        val group = groupService.getGroupByAccessToken(groupAccessToken)
        if (group == null) {
            logger.info("[${accessToken.padStart(80)}]: Group not found.")

            model.addAttribute("isFound", false)
            model.addAttribute("message", "Группа не найдена.")

            return model
        }

        if (group.admin != admin) {
            logger.info("[${accessToken.padStart(80)}]: Group not found.")

            model.addAttribute("isFound", false)
            model.addAttribute("message", "Группа не найдена.")

            return model
        }

        logger.info("[${accessToken.padStart(80)}]: Group found.")
        model.addAttribute("groupAccessToken", group.accessToken)

        val task = taskService.saveTask(name, description, group.accessToken, tests.size.toLong())!!
        logger.info("[${accessToken.padStart(80)}]: Task created.")

        model.addAttribute("isCreated", true)
        model.addAttribute("id", task.id!!)
        model.addAttribute("name", task.name)
        model.addAttribute("groupName", group.name)

        return model
    }

    @GetMapping("/task")
    fun getTaskSolutions(
        @RequestParam accessToken: String,
        @RequestParam groupAccessToken: String,
        @RequestParam taskId: Long,
        model: Model
    ): Any {
        logger.info("[${accessToken.padStart(80)}]: Client trying to get task solutions.")

        val status = validateAdmin(accessToken)
        if (status != WebUserStatuses.ADMIN) {
            logger.info("[${accessToken.padStart(80)}]: Client is not an admin.")
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseMessage(403, "You are not an admin!"))
        }

        logger.info("[${accessToken.padStart(80)}]: Client is an admin.")
        val webUser = webUserService.getWebUserByAccessToken(accessToken)!!
        val admin = adminService.getAdminByWebUser(webUser)!!

        model.addAttribute("accessToken", webUser.accessToken)

        val group = groupService.getGroupByAccessToken(groupAccessToken)
        if (group == null) {
            logger.info("[${accessToken.padStart(80)}]: Group not found.")

            model.addAttribute("isFound", false)
            model.addAttribute("message", "Группа не найдена.")

            return model
        }

        if (group.admin != admin) {
            logger.info("[${accessToken.padStart(80)}]: Group not found.")

            model.addAttribute("isFound", false)
            model.addAttribute("message", "Группа не найдена.")

            return model
        }

        logger.info("[${accessToken.padStart(80)}]: Group found.")
        model.addAttribute("groupAccessToken", group.accessToken)

        val task = taskService.getTaskById(taskId)
        if (task == null) {
            logger.info("[${accessToken.padStart(80)}]: Task not found.")

            model.addAttribute("isFound", false)
            model.addAttribute("message", "Задание не найдено.")

            return model
        }

        if (!task.groups.contains(group)) {
            logger.info("[${accessToken.padStart(80)}]: Task not found.")

            model.addAttribute("isFound", false)
            model.addAttribute("message", "Задание не найдено.")

            return model
        }

        logger.info("[${accessToken.padStart(80)}]: Task found.")
        model.addAttribute("taskId", task.id)
        model.addAttribute("taskName", task.name)

        model.addAttribute("isFound", true)
        model.addAttribute("name", task.name)
        model.addAttribute("groupName", group.name)
        model.addAttribute("solutions", task.solutions.sortedBy { it.id })

        return model
    }

    @GetMapping("/student")
    fun getStudentSubmissions(
        @RequestParam accessToken: String,
        @RequestParam groupAccessToken: String,
        @RequestParam studentId: Long,
        model: Model
    ): Any {
        logger.info("[${accessToken.padStart(80)}]: Client trying to get student submissions.")

        val status = validateAdmin(accessToken)
        if (status != WebUserStatuses.ADMIN) {
            logger.info("[${accessToken.padStart(80)}]: Client is not an admin.")
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseMessage(403, "You are not an admin!"))
        }

        logger.info("[${accessToken.padStart(80)}]: Client is an admin.")
        val webUser = webUserService.getWebUserByAccessToken(accessToken)!!
        val admin = adminService.getAdminByWebUser(webUser)!!

        model.addAttribute("accessToken", webUser.accessToken)

        val group = groupService.getGroupByAccessToken(groupAccessToken)
        if (group == null) {
            logger.info("[${accessToken.padStart(80)}]: Group not found.")

            model.addAttribute("isFound", false)
            model.addAttribute("message", "Группа не найдена.")

            return model
        }

        if (group.admin != admin) {
            logger.info("[${accessToken.padStart(80)}]: Group not found.")

            model.addAttribute("isFound", false)
            model.addAttribute("message", "Группа не найдена.")

            return model
        }

        logger.info("[${accessToken.padStart(80)}]: Group found.")
        model.addAttribute("groupAccessToken", group.accessToken)

        val student = studentService.getStudentById(studentId)
        if (student == null) {
            logger.info("[${accessToken.padStart(80)}]: Student not found.")

            model.addAttribute("isFound", false)
            model.addAttribute("message", "Студент не найден.")

            return model
        }

        if (student.group != group) {
            logger.info("[${accessToken.padStart(80)}]: Student not found.")

            model.addAttribute("isFound", false)
            model.addAttribute("message", "Студент не найден.")

            return model
        }

        logger.info("[${accessToken.padStart(80)}]: Student found.")
        model.addAttribute("studentId", student.id)
        model.addAttribute("studentName", student.webUser.username)

        model.addAttribute("isFound", true)
        model.addAttribute("groupName", group.name)
        model.addAttribute("solutions", student.solutions.sortedBy { it.id })

        return model
    }

    private fun validateAdmin(accessToken: String): WebUserStatuses {
        val webUser = webUserService.getWebUserByAccessToken(accessToken) ?: return WebUserStatuses.NOT_FOUND
        adminService.getAdminByWebUser(webUser) ?: return WebUserStatuses.WEB_USER

        return WebUserStatuses.ADMIN
    }
}