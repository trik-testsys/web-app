package trik.testsys.webclient.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

import trik.testsys.webclient.models.ResponseMessage
import trik.testsys.webclient.service.*
import trik.testsys.webclient.util.logger.TrikLogger
import trik.testsys.webclient.entity.Admin
import trik.testsys.webclient.entity.Group
import trik.testsys.webclient.entity.Solution
import trik.testsys.webclient.entity.WebUser
import trik.testsys.webclient.util.fp.Either


@RestController
@RequestMapping("\${app.testsys.api.prefix}/admin")
class AdminController @Autowired constructor(
    @Value("\${app.grading-system.path}")
    private val gradingSystemUrl: String,

    private val adminService: AdminService,
    private val webUserService: WebUserService,
    private val groupService: GroupService,
    private val taskService: TaskService,
    private val studentService: StudentService
) {

    private val restTemplate = RestTemplate()

    @GetMapping
    fun getAccess(@RequestParam accessToken: String, model: Model): Any {
        logger.info("[${accessToken.padStart(80)}]: Client trying to access admin page.")

        val isAdmin = isAdminAccessToken(accessToken)
        if (isAdmin) {
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

        val isAdmin = isAdminAccessToken(accessToken)
        if (!isAdmin) {
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
        logger.info("[${accessToken.padStart(80)}]: Group created.")

        model.addAttribute("isCreated", true)
        model.addAttribute("id", group.id!!)
        model.addAttribute("name", group.name)
        model.addAttribute("groupAccessToken", group.accessToken)

        return model
    }

    @GetMapping("/group")
    fun accessToGroup(@RequestParam accessToken: String, @RequestParam groupAccessToken: String, model: Model): Any {
        logger.info("[${accessToken.padStart(80)}]: Client trying to access group.")

        val eitherEntities = getAdminEntities(accessToken, groupAccessToken)
        if (eitherEntities.isLeft()) {
            return eitherEntities.getLeft()
        }
        val (webUser, _, group) = eitherEntities.getRight()

        model.addAttribute("accessToken", webUser.accessToken)
        model.addAttribute("isFound", true)
        model.addAttribute("id", group.id!!)
        model.addAttribute("name", group.name)
        model.addAttribute("groupAccessToken", group.accessToken)
        model.addAttribute("tasks", group.tasks.sortedBy { it.id })
        model.addAttribute("students", group.students.sortedBy { it.id })

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

        val eitherEntities = getAdminEntities(accessToken, groupAccessToken)
        if (eitherEntities.isLeft()) {
            return eitherEntities.getLeft()
        }
        val (webUser, _, group) = eitherEntities.getRight()

        model.addAttribute("accessToken", webUser.accessToken)
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

        val eitherEntities = getAdminEntities(accessToken, groupAccessToken)
        if (eitherEntities.isLeft()) {
            return eitherEntities.getLeft()
        }
        val (webUser, _, group) = eitherEntities.getRight()

        model.addAttribute("accessToken", webUser.accessToken)
        model.addAttribute("groupAccessToken", group.accessToken)

        val student = studentService.getById(studentId)
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

    @GetMapping("/group/table")
    fun getGroupTable(@RequestParam accessToken: String, @RequestParam groupAccessToken: String, model: Model): Any {
        logger.info("[${accessToken.padStart(80)}]: Client trying to get group table.")

        val eitherEntities = getAdminEntities(accessToken, groupAccessToken)
        if (eitherEntities.isLeft()) {
            return eitherEntities.getLeft()
        }
        val (webUser, _, group) = eitherEntities.getRight()

        model.addAttribute("accessToken", webUser.accessToken)
        model.addAttribute("groupAccessToken", group.accessToken)

        model.addAttribute("isFound", true)
        model.addAttribute("tasks", group.tasks.sortedBy { it.id })
        model.addAttribute("groupName", group.name)
        model.addAttribute("students", group.students.sortedBy { it.id })

        if (group.tasks.isEmpty()) {
            model.addAttribute("table", emptyList<Int>())
            return model
        }

        val table = MutableList<MutableList<Int>>(
            (group.students.sortedByDescending { it.id }.firstOrNull()?.id?.toInt()?.plus(1)) ?: 0
        ) { mutableListOf() }

        group.students.sortedByDescending { it.id }.forEach { student ->
            val taskList = MutableList(
                group.tasks.sortedByDescending { it.id }.firstOrNull()?.id?.toInt()?.plus(1) ?: 0
            ) { -1 }

            group.tasks.sortedByDescending { it.id }.forEach { task ->
                logger.warn("Task: $task")

                val failedSolution =
                    student.solutions.find { it.task == task && (it.status == Solution.Status.FAILED || it.status == Solution.Status.ERROR) }
                val passedSolution =
                    student.solutions.find { it.task == task && it.status == Solution.Status.PASSED }
                val inProgressSolution =
                    student.solutions.find { it.task == task && (it.status == Solution.Status.IN_PROGRESS || it.status == Solution.Status.NOT_STARTED) }

                if (failedSolution != null) {
                    taskList[task.id!!.toInt()] = 0
                } else if (passedSolution != null) {
                    taskList[task.id!!.toInt()] = 1
                } else if (inProgressSolution != null) {
                    taskList[task.id!!.toInt()] = 2
                } else {
                    taskList[task.id!!.toInt()] = -1
                }

                table[student.id!!.toInt()] = taskList
            }
        }
        model.addAttribute("table", table.toList())

        return model
    }

    @GetMapping("/student/create")
    @ResponseBody
    fun createStudents(
        @RequestParam accessToken: String,
        @RequestParam groupAccessToken: String,
        @RequestParam count: Long,
        @RequestParam studentAccessTokenPrefix: String,
        @RequestParam namePrefix: String,
        model: Model
    ): ResponseEntity<out Any> {
        logger.info(accessToken, "Client trying to create many students.")

        val eitherEntities = getAdminEntities(accessToken, groupAccessToken)
        if (eitherEntities.isLeft()) {
            return eitherEntities.getLeft()
        }
        val (_, _, group) = eitherEntities.getRight()

        val students = studentService.generateStudents(count, studentAccessTokenPrefix, namePrefix, group)
        logger.info(accessToken, "$count students created.")

        val csvFile = studentService.convertToCsv(students)
        logger.info(accessToken, "CSV with created students created.")
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        headers.contentDisposition = ContentDisposition.builder("attachment")
            .filename("students.csv")
            .build()
        val bytes = csvFile.readBytes()

        return ResponseEntity
            .ok()
            .headers(headers)
            .contentLength(bytes.size.toLong())
            .body(FileSystemResource(csvFile))
    }

    /**
     * @author Roman Shishkin
     * @since 1.1.0
     */
    private fun isAdminAccessToken(accessToken: String): Boolean {
        val webUser = webUserService.getWebUserByAccessToken(accessToken) ?: run {
            logger.info(accessToken, "Client not found.")
            return false
        }
        adminService.getAdminByWebUser(webUser) ?: run {
            logger.info(accessToken, "Client is not an admin.")
            return false
        }

        logger.info(accessToken, "Client is an admin.")
        return true
    }

    /**
     * Validates if [accessToken] belongs to an [Admin].
     * @return null if client is an admin, else [ResponseEntity] with status 403 and message "You are not an admin!".
     * @author Roman Shishkin
     * @since 1.1.0
     */
    private fun validateAdminByAccessToken(accessToken: String): ResponseEntity<ResponseMessage>? {
        if (isAdminAccessToken(accessToken)) return null

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ResponseMessage(403, "You are not an admin!"))
    }

    /**
     * Validates if [accessToken] belongs to a [Group].
     * @return [Either] with [ResponseEntity] with status 404 and message "Group not found!" if group not found,
     * else [Either] with null and [Group] if group found.
     * @see Either
     * @author Roman Shishkin
     * @since 1.1.0
     */
    private fun validateGroupAccessToken(
        accessToken: String,
        admin: Admin
    ): Either<ResponseEntity<ResponseMessage>, Group> {
        val group = groupService.getGroupByAccessToken(accessToken) ?: run {
            logger.info(accessToken, "Group not found.")

            val responseEntity = ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseMessage(404, "Group not found!"))

            return Either.left(responseEntity)
        }

        if (group.admin != admin) {
            logger.info(accessToken, "Group not found.")

            val responseEntity = ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseMessage(404, "Group not found!"))

            return Either.left(responseEntity)
        }

        logger.info(accessToken, "Group found.")
        return Either.right(group)
    }

    /**
     * Validates everything belongs to an [Admin] by [accessToken] and [groupAccessToken].
     * @return [Either] with [ResponseEntity], else [Either] with null and [Entities].
     * @see Either
     * @see Entities
     * @author Roman Shishkin
     */
    private fun getAdminEntities(
        accessToken: String,
        groupAccessToken: String
    ): Either<ResponseEntity<ResponseMessage>, Entities> {
        logger.info(accessToken, "Validating admin access token.")
        validateAdminByAccessToken(accessToken)?.let { responseEntity ->
            return Either.left(responseEntity)
        }

        val webUser = webUserService.getWebUserByAccessToken(accessToken)
            ?: logger.errorAndThrow(accessToken, "Web user not found.")

        val admin = adminService.getAdminByWebUser(webUser)
            ?: logger.errorAndThrow(accessToken, "Admin not found.")

        val eitherGroup = validateGroupAccessToken(groupAccessToken, admin)
        return eitherGroup.bind { group ->
            val entities = Entities(webUser, admin, group)
            Either.right(entities)
        }
    }

    private data class Entities(
        val webUser: WebUser,
        val admin: Admin,
        val group: Group
    )

    companion object {
        private val logger = TrikLogger(this::class.java)
    }
}