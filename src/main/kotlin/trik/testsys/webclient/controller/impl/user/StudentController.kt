package trik.testsys.webclient.controller.impl.user

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import trik.testsys.webclient.controller.impl.main.LoginController
import trik.testsys.webclient.controller.user.UserController
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.service.entity.user.impl.StudentService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.StudentView
import java.io.File
import java.util.TimeZone


@Controller
@RequestMapping(StudentController.STUDENT_PATH)
class StudentController(
    loginData: LoginData,
) : UserController<Student, StudentView, StudentService>(loginData) {

    override val MAIN_PATH = STUDENT_PATH

    override val MAIN_PAGE = STUDENT_PAGE

    @ResponseBody
    @GetMapping("/task/download")
    fun downloadTask(
        redirectAttributes: RedirectAttributes,
        model: Model
    ): Any {
        validate(redirectAttributes) ?: return RedirectView(LoginController.LOGIN_PATH)
        val file = File("/Users/shisha/Projects/Kotlin/trik-testsys-web-client2/Dockerfile")

        val responseEntity = ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"task.qrs\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file.readBytes())

        return responseEntity
    }

    override fun Student.toView(timeZone: TimeZone) = StudentView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        creationDate = this.creationDate?.atTimeZone(timeZone),
        lastLoginDate = this.lastLoginDate.atTimeZone(timeZone),
        group = this.group,
        additionalInfo = this.additionalInfo
    )

    companion object {

        internal const val STUDENT_PAGE = "student"

        internal const val STUDENT_PATH = "/student"
    }
}


//
//class StudentController
//
//    @GetMapping("/registration")
//    fun registration(@RequestParam groupAccessToken: String, model: Model): Any {
//        logger.info("[${groupAccessToken.padStart(80)}]: Client trying to register.")
//
//        val group = groupService.getGroupByAccessToken(groupAccessToken) ?: run {
//            logger.info("[${groupAccessToken.padStart(80)}]: Invalid  group token.")
//
//            return ResponseEntity
//                .status(HttpStatus.FORBIDDEN)
//                .body(ResponseMessage(403, "Invalid group token!"))
//        }
//
//        if (!group.isAccessible) {
//            logger.info("[${groupAccessToken.padStart(80)}]: Group is not accessible.")
//
//            return ResponseEntity
//                .status(HttpStatus.FORBIDDEN)
//                .body(ResponseMessage(403, "Group is not accessible!"))
//        }
//
//
//        logger.info("[${groupAccessToken.padStart(80)}]: Group token is valid.")
//
//        model.addAttribute("groupAccessToken", groupAccessToken)
//        return model
//    }
//
//    @PostMapping("/create")
//    fun createStudent(@RequestParam groupAccessToken: String, @RequestParam username: String, model: Model): Any {
//        logger.info("[${groupAccessToken.padStart(80)}]: Client trying to create student.")
//
//        val group = groupService.getGroupByAccessToken(groupAccessToken) ?: run {
//            logger.info("[${groupAccessToken.padStart(80)}]: Invalid  group token.")
//
//            return ResponseEntity
//                .status(HttpStatus.FORBIDDEN)
//                .body(ResponseMessage(403, "Invalid group token!"))
//        }
//
//        if (!group.isAccessible) {
//            logger.info("[${groupAccessToken.padStart(80)}]: Group is not accessible.")
//
//            return ResponseEntity
//                .status(HttpStatus.FORBIDDEN)
//                .body(ResponseMessage(403, "Group is not accessible!"))
//        }
//
//        logger.info("[${groupAccessToken.padStart(80)}]: Group token is valid.")
//
//        val webUser = webUserService.saveWebUser(username)
//        logger.info("[${groupAccessToken.padStart(80)}]: Web user created.")
//
//        val student = studentService.save(webUser, group)
//        logger.info("[${groupAccessToken.padStart(80)}]: Student created.")
//
//        model.addAttribute("id", student.id)
//        model.addAttribute("username", username)
//        model.addAttribute("accessToken", webUser.accessToken)
//        return model
//    }
//
//    @GetMapping("/task")
//    fun getTask(@RequestParam accessToken: String, @RequestParam taskId: Long, model: Model): Any {
//        logger.info("[${accessToken.padStart(80)}]: Client trying to access task page.")
//
//        val status = validateStudent(accessToken)
//        if (status == WebUser.Status.NOT_FOUND || status == WebUser.Status.ADMIN) {
//            logger.info("[${accessToken.padStart(80)}]: Client is not a student.")
//            return ResponseEntity
//                .status(HttpStatus.FORBIDDEN)
//                .body(ResponseMessage(403, "You are not a student!"))
//        }
//
//        logger.info("[${accessToken.padStart(80)}]: Client is a student.")
//        val webUser = webUserService.getWebUserByAccessToken(accessToken)!!
//        val student = studentService.getByWebUser(webUser)!!
//        val group = student.group
//
//        if (!group.isAccessible) {
//            logger.info("[${group.accessToken.padStart(80)}]: Group is not accessible.")
//
//            return ResponseEntity
//                .status(HttpStatus.FORBIDDEN)
//                .body(ResponseMessage(403, "Group is not accessible!"))
//        }
//
//        val task = taskService.getTaskById(taskId) ?: run {
//            logger.info("[${accessToken.padStart(80)}]: Invalid task id.")
//            return ResponseEntity
//                .status(HttpStatus.FORBIDDEN)
//                .body(ResponseMessage(403, "Invalid task id!"))
//        }
//
//        val trainingFile = File("$trainingPath/$taskId.qrs")
//        if (!trainingFile.exists()) {
//            logger.info("[${accessToken.padStart(80)}]: Training file not found.")
//            return ResponseEntity
//                .status(HttpStatus.FORBIDDEN)
//                .body(ResponseMessage(403, "Training file not found!"))
//        }
//
//        val responseEntity = ResponseEntity.ok()
//            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${task.name}.qrs\"")
//            .contentType(MediaType.APPLICATION_OCTET_STREAM)
//            .body(trainingFile.readBytes())
//
//        //region TaskAction creation
//        logger.info("[${accessToken.padStart(80)}]: Creating task action.")
//        taskActionService.getDownloadedTrainingAction(student, task)?.let {
//            logger.info("[${accessToken.padStart(80)}]: Task action already exists ${it.id}.")
//            return responseEntity
//        }
//        val taskAction = taskActionService.save(student, task)
//
//        logger.info("[${accessToken.padStart(80)}]: Task action created ${taskAction.id}.")
//        //endregion
//
//        logger.info("[${accessToken.padStart(80)}]: Training file sent.")
//
//        val taskTimes = generateTaskTimes(student, student.group.tasks)
//        val minTaskTime = taskTimes.minOfOrNull { it.value } ?: LocalDateTime.MIN
//        model.addAttribute("taskTimes", minTaskTime)
//        model.addAttribute("canUpload", minTaskTime != LocalDateTime.MIN)
//
//        return responseEntity
//    }
//
//    @PostMapping("/solution/upload")
//    fun uploadSolution(
//        @RequestParam accessToken: String,
//        @RequestParam taskId: Long,
//        @RequestBody file: MultipartFile,
//        model: Model
//    ): Any {
//        restTemplate.errorHandler = GradingSystemErrorHandler()
//        logger.info("[${accessToken.padStart(80)}]: Client trying to upload solution.")
//
//        val status = validateStudent(accessToken)
//        if (status == WebUser.Status.NOT_FOUND || status == WebUser.Status.ADMIN) {
//            logger.info("[${accessToken.padStart(80)}]: Client is not a student.")
//            return ResponseEntity
//                .status(HttpStatus.FORBIDDEN)
//                .body(ResponseMessage(403, "You are not a student!"))
//        }
//
//        logger.info("[${accessToken.padStart(80)}]: Client is a student.")
//        model.addAttribute("accessToken", accessToken)
//
//        val webUser = webUserService.getWebUserByAccessToken(accessToken)!!
//        val student = studentService.getByWebUser(webUser)!!
//        val group = student.group
//
//        if (!group.isAccessible) {
//            logger.info("[${group.accessToken.padStart(80)}]: Group is not accessible.")
//
//            return ResponseEntity
//                .status(HttpStatus.FORBIDDEN)
//                .body(ResponseMessage(403, "Group is not accessible!"))
//        }
//
//        val task = taskService.getTaskById(taskId) ?: run {
//            logger.info("[${accessToken.padStart(80)}]: Invalid task id.")
//            return ResponseEntity
//                .status(HttpStatus.FORBIDDEN)
//                .body(ResponseMessage(403, "Invalid task id!"))
//        }
//        val deadline = task.deadline
//        if (deadline != null && deadline < LocalDateTime.now()) {
//            logger.info("[${accessToken.padStart(80)}]: Deadline is over.")
//            //TODO: add normal page
//            return ResponseEntity
//                .status(HttpStatus.FORBIDDEN)
//                .body(ResponseMessage(403, "Deadline is over!"))
//        }
//
//        val taskName = "${task.id}: ${task.name}"
//
//        val headers = HttpHeaders()
//        headers.setBasicAuth("user1", "super")
//        // 73d1b1b1bc1dabfb97f216d897b7968e44b06457920f00f2dc6c1ed3be25ad4c
//        headers.contentType = MediaType.MULTIPART_FORM_DATA
//
//        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
//        body.add("file", file.resource)
//        body.add("task_name", taskName)
//        body.add("student_id", student.id!!)
//
//        val url = "$gradingSystemUrl/submissions/submission/upload"
//
//        val submissionInfo = restTemplate.postForEntity(
//            url,
//            HttpEntity(body, headers),
//            Map::class.java
//        )
//
//        if (submissionInfo.statusCodeValue == 422) {
//            logger.info("[${accessToken.padStart(80)}]: Invalid file.")
//            model.addAttribute("isCreated", false)
//            model.addAttribute(
//                "message",
//                "Вы отправили некорректный формат файла. Попробуйте отправить файл с расширением .qrs."
//            )
//            return model
//        }
//
//        if (submissionInfo.statusCode == HttpStatus.INTERNAL_SERVER_ERROR) {
//            logger.info("[${accessToken.padStart(80)}]: Something went wrong on grading system server.")
//            model.addAttribute("isCreated", false)
//            model.addAttribute(
//                "message",
//                "Что-то пошло не так на сервере системы проверки. Обратитесь к администратору и попробуйте позже."
//            )
//            return model
//        }
//        model.addAttribute("isCreated", true)
//
//        val solution =
//            solutionService.saveSolution(student.id, taskId, submissionInfo.body!!["id"].toString().toLong())!!
//        logger.trace("[ Uploading solution ] -- Solution id: ${solution.id}, Submission id: ${solution.gradingId}.")
//        logger.info("[${accessToken.padStart(80)}]: Solution uploaded.")
//
//        //region TaskAction creation
//        logger.info("[${accessToken.padStart(80)}]: Creating task action.")
//        val taskAction = taskActionService.save(student, solution)
//
//        logger.info("[${accessToken.padStart(80)}]: Task action created ${taskAction.id}.")
//        //endregion
//
//        model.addAttribute("id", solution.id)
//        model.addAttribute("taskName", solution.task.name)
//        return model
//    }
//
//    private val timeToSolve = 90 * 60
//    private val dateTime = LocalDateTime.MIN.plusHours(1).plusMinutes(30)
//
//    private fun generateTaskTimes(student: Student, tasks: Collection<Task>): Map<Long, LocalDateTime> {
//        val currentTime = LocalDateTime.now(UTC).toEpochSecond(UTC)
//
//        logger.info("Current time: ${LocalDateTime.now(UTC)}")
//        logger.info("Date time: ${LocalDateTime.MIN.plusHours(1).plusMinutes(30)}")
//
//        val taskTimes = mutableMapOf<Long, LocalDateTime>()
//        tasks.forEach { task ->
//            val taskAction = taskActionService.getDownloadedTrainingAction(student, task) ?: run {
//                taskTimes[task.id!!] = dateTime
//                return@forEach
//            }
//
//            logger.info("Task action date time: ${taskAction.dateTime}")
//
//            val spentTime = currentTime - taskAction.dateTime.toEpochSecond(UTC)
//            if (spentTime > timeToSolve) {
//                taskTimes[task.id!!] = LocalDateTime.MIN
//            } else {
//                val hours = TimeUnit.SECONDS.toHours(timeToSolve - spentTime)
//                val minutes = TimeUnit.SECONDS.toMinutes(timeToSolve - spentTime) - TimeUnit.HOURS.toMinutes(hours)
//                val seconds = TimeUnit.SECONDS.toSeconds(timeToSolve - spentTime) -
//                        TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours)
//
//                logger.info("Hours: $hours, minutes: $minutes, seconds: $seconds")
//
//                taskTimes[task.id!!] = LocalDateTime.MIN.plusHours(hours).plusMinutes(minutes).plusSeconds(seconds)
//            }
//
//            logger.info("Task time: ${taskTimes[task.id]}")
//        }
//        return taskTimes
//    }
//}