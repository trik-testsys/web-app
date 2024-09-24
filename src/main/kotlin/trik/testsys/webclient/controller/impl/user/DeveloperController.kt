package trik.testsys.webclient.controller.impl.user

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import trik.testsys.webclient.controller.user.UserController
import trik.testsys.webclient.entity.user.impl.Developer
import trik.testsys.webclient.service.entity.user.impl.DeveloperService
import trik.testsys.webclient.service.security.UserValidator
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.DeveloperView
import java.util.*


@Controller
@RequestMapping(DeveloperController.DEVELOPER_PATH)
class DeveloperController(
    loginData: LoginData
) : UserController<Developer, DeveloperView, DeveloperService>(loginData) {

    override val MAIN_PATH = DEVELOPER_PATH

    override val MAIN_PAGE = DEVELOPER_PAGE

    override fun Developer.toView(timeZone: TimeZone) = DeveloperView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        lastLoginDate = this.lastLoginDate.atTimeZone(timeZone),
        creationDate = this.creationDate?.atTimeZone(timeZone)
    )

    companion object {

        const val DEVELOPER_PATH = "/developer"

        const val DEVELOPER_PAGE = "developer"
    }
}

//    @PostMapping("/task/create")
//    fun createTask(
//        @RequestParam accessToken: String,
//        @RequestParam name: String,
//        @RequestParam description: String,
//        @RequestBody tests: List<MultipartFile>,
//        @RequestBody benchmark: MultipartFile?,
//        @RequestBody training: MultipartFile?,
//        modelAndView: ModelAndView
//    ): ModelAndView {
//        logger.info(accessToken, "Client trying to create task.")
//
//        val eitherDeveloperEntities = validateDeveloper(accessToken)
//        if (eitherDeveloperEntities.isLeft()) {
//            return eitherDeveloperEntities.getLeft()
//        }
//        modelAndView.view = REDIRECT_VIEW
//
//        val (developer, webUser) = eitherDeveloperEntities.getRight()
//        val developerModelBuilder = DeveloperModel.Builder()
//            .accessToken(accessToken)
//            .username(webUser.username)
//
//        modelAndView.addObject("accessToken", accessToken)
//
//        logger.info(accessToken, "Saving task $name.")
//        val task = taskService.saveTask(name, description, developer, tests, training, benchmark)
//
//        postTask("${task.id}: $name", tests)
////        val isTaskPosted = postTask(name, tests, benchmark, training)
////        if (!isTaskPosted) {
////            developerModelBuilder.postTaskMessage("Задача '${task.fullName}' не была загружена на сервер, попробуйте еще раз.")
////            val developerModel = developerModelBuilder.build()
////            modelAndView.addAllObjects(developerModel.asMap())
////
////            return modelAndView
////        }
//        logger.info(accessToken, "Task '${task.getFullName()}' was successfully posted.")
//
//        //region Training task saving
//        logger.info(accessToken, "Saving training file for task ${task.getFullName()}.")
//
//        val trainingFile = File("$trainingPath/${task.id}.qrs")
//        trainingFile.createNewFile()
//        training?.transferTo(trainingFile) ?: trainingFile.writeBytes(byteArrayOf())
//
//        logger.info(accessToken, "Training file for task ${task.getFullName()} was successfully saved.")
//        //endregion
//
//        //region Training task saving
//        logger.info(accessToken, "Saving benchmark file for task ${task.getFullName()}.")
//
//        val benchmarkFile = File("$benchmarkPath/${task.id}.qrs")
//        benchmarkFile.createNewFile()
//        benchmark?.transferTo(benchmarkFile) ?: benchmarkFile.writeBytes(byteArrayOf())
//
//        logger.info(accessToken, "Benchmark file for task ${task.getFullName()} was successfully saved.")
//        //endregion
//
//        developerModelBuilder.postTaskMessage("Задача '${task.getFullName()}' была успешно загружена на сервер.")
//        developerModelBuilder.tasks(developer.tasks)
//        developerModelBuilder.admins(adminService.getAll())
//        val developerModel = developerModelBuilder.build()
//        modelAndView.addAllObjects(developerModel.asMap())
//
//        return modelAndView
//    }
//
//    @PostMapping("/task/delete")
//    fun deleteTask(
//        @RequestParam accessToken: String,
//        @RequestParam taskId: Long,
//        modelAndView: ModelAndView
//    ): ModelAndView {
//        logger.info(accessToken, "Client trying to delete task.")
//
//        val eitherDeveloperEntities = validateDeveloper(accessToken)
//        if (eitherDeveloperEntities.isLeft()) {
//            return eitherDeveloperEntities.getLeft()
//        }
//        modelAndView.view = REDIRECT_VIEW
//
//        val (developer, webUser) = eitherDeveloperEntities.getRight()
//        val developerModelBuilder = DeveloperModel.Builder()
//            .accessToken(accessToken)
//            .username(webUser.username)
//
//        modelAndView.addObject("accessToken", accessToken)
//
//        val task = taskService.getTaskById(taskId) ?: run {
//            developerModelBuilder.postTaskMessage("Задача с id '${taskId}' не найдена.")
//            val developerModel = developerModelBuilder
//                .tasks(developer.tasks)
//                .admins(adminService.getAll())
//                .build()
//            modelAndView.addAllObjects(developerModel.asMap())
//
//            return modelAndView
//        }
//
//        val isTaskDeleted = taskService.deleteTask(taskId)
//        if (!isTaskDeleted) {
//            developerModelBuilder.postTaskMessage("Задача '${task.getFullName()}' не была удалена с сервера, попробуйте еще раз.")
//            val developerModel = developerModelBuilder.build()
//            modelAndView.addAllObjects(developerModel.asMap())
//
//            return modelAndView
//        }
//        logger.info(accessToken, "Task '${task.getFullName()}' was successfully deleted.")
//
//        developerModelBuilder.postTaskMessage("Задача '${task.getFullName()}' была успешно удалена с сервера.")
//        val developerModel = developerModelBuilder
//            .tasks(developer.tasks)
//            .admins(adminService.getAll())
//            .build()
//        modelAndView.addAllObjects(developerModel.asMap())
//
//        return modelAndView
//    }
//
//    @PostMapping("/task/changeDeadline")
//    fun changeTaskDeadline(
//        @RequestParam accessToken: String,
//        @RequestParam taskId: Long,
//        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") deadline: LocalDateTime,
//        modelAndView: ModelAndView
//    ): ModelAndView {
//        logger.info(accessToken, "Client trying to create task.")
//
//        val eitherDeveloperEntities = validateDeveloper(accessToken)
//        if (eitherDeveloperEntities.isLeft()) {
//            return eitherDeveloperEntities.getLeft()
//        }
//        modelAndView.viewName = DEVELOPER_VIEW_NAME
//
//        val (developer, webUser) = eitherDeveloperEntities.getRight()
//
//        val task = taskService.getTaskById(taskId)
//        if (task != null) {
//            val serverDeadline = deadline.minusHours(UTC_OFFSET)
//            task.deadline = serverDeadline
//            taskService.saveTask(task)
//        }
//
//        val developerModel = getModel(developer)
//
//        modelAndView.addAllObjects(developerModel.asMap())
//        return modelAndView
//    }
//
//    private fun postTask(
//        name: String,
//        tests: List<MultipartFile>
//    ): Boolean {
//        val restTemplate = RestTemplate()
//        restTemplate.errorHandler = GradingSystemErrorHandler()
//
//        val headers = HttpHeaders()
//        headers.contentType = MediaType.MULTIPART_FORM_DATA
//        headers.setBasicAuth("admin", "@dm1n") // TODO("Change to real credentials.")
//        // a35c5f63916fff41369754c7a01cc4a82e9e3e5f1e05628791b5f5770435d6b0
//        val body = LinkedMultiValueMap<String, Any>()
//        body.add("taskName", name)
//
//        tests.forEach { body.add("files", it.resource) }
////        benchmark?.let { body.add("benchmark", it.resource) }
////        training?.let { body.add("training", it.resource) }
//
//        val url = "$gradingSystemUrl/tasks/create"
//        val responseInfo = restTemplate.postForEntity(
//            url,
//            HttpEntity(body, headers),
//            Map::class.java
//        )
//
//        if (responseInfo.statusCode != HttpStatus.OK) {
//            logger.error("Error while creating task '$name': ${responseInfo.statusCode}")
//            return false
//        }
//
//        return true
//    }
//
//    private fun changeTask(
//        name: String,
//        tests: List<MultipartFile>
//    ): Boolean  {
//        val restTemplate = RestTemplate()
//        restTemplate.errorHandler = GradingSystemErrorHandler()
//
//        val headers = HttpHeaders()
//        headers.contentType = MediaType.MULTIPART_FORM_DATA
//        headers.setBasicAuth("admin", "@dm1n") // TODO("Change to real credentials.")
//
//        val body = LinkedMultiValueMap<String, Any>()
//        body.add("taskName", name)
//
//        tests.forEach { body.add("files", it.resource) }
////        benchmark?.let { body.add("benchmark", it.resource) }
////        training?.let { body.add("training", it.resource) }
//
//        val url = "$gradingSystemUrl/tasks/change"
//        val responseInfo = restTemplate.postForEntity(
//            url,
//            HttpEntity(body, headers),
//            Map::class.java
//        )
//
//        if (responseInfo.statusCode != HttpStatus.OK) {
//            logger.error("Error while changing task '$name': ${responseInfo.statusCode}")
//            return false
//        }
//
//        return true
//    }
//
//    @PostMapping("/task/edit")
//    fun editTask(
//        @RequestParam accessToken: String,
//        @RequestParam taskId: Long,
//        @RequestParam name: String,
//        @RequestParam description: String,
//        @RequestBody tests: List<MultipartFile>,
//        @RequestBody benchmark: MultipartFile?,
//        @RequestBody training: MultipartFile?,
//        modelAndView: ModelAndView
//    ): ModelAndView {
//        logger.info(accessToken, "Client trying to update task.")
//
//        val eitherDeveloperEntities = validateDeveloper(accessToken)
//        if (eitherDeveloperEntities.isLeft()) {
//            return eitherDeveloperEntities.getLeft()
//        }
//        modelAndView.view = REDIRECT_VIEW
//
//        val (developer, webUser) = eitherDeveloperEntities.getRight()
//        val developerModelBuilder = DeveloperModel.Builder()
//            .accessToken(accessToken)
//            .username(webUser.username)
//
//        modelAndView.addObject("accessToken", accessToken)
//
//        val testsCount = tests.size.toLong()
//        val task = taskService.update(taskId, name, description, tests, training, benchmark) ?: run {
//            logger.warn(accessToken, "Task with id '$taskId' not found.")
//
//            developerModelBuilder.postTaskMessage("Задача с id '$taskId' не найдена.")
//            val developerModel = developerModelBuilder.build()
//            modelAndView.addAllObjects(developerModel.asMap())
//
//            return modelAndView
//        }
//
//        changeTask("${task.id}: $name", tests)
////        val isTaskPosted = postTask(name, tests, benchmark, training)
////        if (!isTaskPosted) {
////            developerModelBuilder.postTaskMessage("Задача '${task.fullName}' не была загружена на сервер, попробуйте еще раз.")
////            val developerModel = developerModelBuilder.build()
////            modelAndView.addAllObjects(developerModel.asMap())
////
////            return modelAndView
////        }
//        logger.info(accessToken, "Task '${task.getFullName()}' was successfully updated.")
//
//        //region Training task saving
//        logger.info(accessToken, "Changing training file for task ${task.getFullName()}.")
//
//        val trainingFile = File("$trainingPath/${task.id}.qrs")
//        training ?.let {
//            trainingFile.delete()
//            trainingFile.createNewFile()
//
//            training.transferTo(trainingFile)
//        }
//
//        logger.info(accessToken, "Training file for task ${task.getFullName()} was successfully changed.")
//        //endregion
//
//        //region Training task saving
//        logger.info(accessToken, "Changing benchmark file for task ${task.getFullName()}.")
//
//        val benchmarkFile = File("$benchmarkPath/${task.id}.qrs")
//        benchmark ?.let {
//            benchmarkFile.delete()
//            benchmarkFile.createNewFile()
//
//            benchmark.transferTo(benchmarkFile)
//        }
//
//        logger.info(accessToken, "Benchmark file for task ${task.getFullName()} was successfully changed.")
//        //endregion
//
//        developerModelBuilder.postTaskMessage("Задача '${task.getFullName()}' была успешно загружена на сервер.")
//        developerModelBuilder.tasks(developer.tasks)
//        developerModelBuilder.admins(adminService.getAll())
//        val developerModel = developerModelBuilder.build()
//        modelAndView.addAllObjects(developerModel.asMap())
//
//        return modelAndView
//    }
//
//    @PostMapping("/task/attach")
//    fun attachTaskToAdmin(
//        @RequestParam accessToken: String,
//        @RequestParam taskId: Long,
//        @RequestParam adminIds: List<Long>,
//        modelAndView: ModelAndView
//    ): ModelAndView {
//        logger.info(accessToken, "Client trying to attach task to admins.")
//
//        val eitherDeveloperEntities = validateDeveloper(accessToken)
//        if (eitherDeveloperEntities.isLeft()) {
//            return eitherDeveloperEntities.getLeft()
//        }
//        modelAndView.view = REDIRECT_VIEW
//
//        val (developer, webUser) = eitherDeveloperEntities.getRight()
//
//        val task = taskService.getTaskById(taskId)
//        if (task == null) {
//            logger.warn(accessToken, "Task with id '$taskId' not found.")
//
//            val postTaskMessage = "Задача с id '$taskId' не найдена."
//
//            val developerModel = getModel(developer, postTaskMessage)
//            modelAndView.addAllObjects(developerModel.asMap())
//
//            return modelAndView
//        }
//
//        val admins = adminService.getAllByIds(adminIds)
//        task.admins.addAll(admins)
//        taskService.saveTask(task)
//
//        logger.info(accessToken, "Task '${task.getFullName()}' was successfully attached to admins.")
//
//        val postTaskMessage = "Задача '${task.getFullName()}' была успешно прикреплена к администраторам."
//
//        val developerModel = getModel(developer, postTaskMessage)
//        modelAndView.addAllObjects(developerModel.asMap())
//
//        return modelAndView
//    }
//
//    @PostMapping("/task/detach")
//    fun detachTaskToAdmin(
//        @RequestParam accessToken: String,
//        @RequestParam taskId: Long,
//        @RequestParam adminIds: List<Long>,
//        modelAndView: ModelAndView
//    ): ModelAndView {
//        logger.info(accessToken, "Client trying to detach task from admins.")
//
//        val eitherDeveloperEntities = validateDeveloper(accessToken)
//        if (eitherDeveloperEntities.isLeft()) {
//            return eitherDeveloperEntities.getLeft()
//        }
//        modelAndView.view = REDIRECT_VIEW
//
//        val (developer, webUser) = eitherDeveloperEntities.getRight()
//
//        val task = taskService.getTaskById(taskId)
//        if (task == null) {
//            logger.warn(accessToken, "Task with id '$taskId' not found.")
//
//            val postTaskMessage = "Задача с id '$taskId' не найдена."
//
//            val developerModel = getModel(developer, postTaskMessage)
//            modelAndView.addAllObjects(developerModel.asMap())
//
//            return modelAndView
//        }
//
//        val admins = adminService.getAllByIds(adminIds)
//        task.admins.removeAll(admins.toSet())
//        taskService.saveTask(task)
//
//        admins.forEach { admin ->
//            admin.tasks.remove(task)
//
//            val groups = admin.groups
//            groups.forEach { group ->
//                group.tasks.remove(task)
//            }
//            groupService.saveAll(groups)
//        }
//        adminService.saveAll(admins)
//
//        logger.info(accessToken, "Task '${task.getFullName()}' was successfully detached from admins.")
//
//        val postTaskMessage = "Задача '${task.getFullName()}' была успешно откреплена от администраторов."
//
//        val developerModel = getModel(developer, postTaskMessage)
//        modelAndView.addAllObjects(developerModel.asMap())
//
//        return modelAndView
//    }
//
//    @PostMapping("/task/visibility/change")
//    fun changeVisibility(
//        @RequestParam accessToken: String,
//        @RequestParam taskId: Long,
//        @RequestParam isPublic: Boolean,
//        modelAndView: ModelAndView
//    ): ModelAndView {
//        logger.info(accessToken, "Developer trying to make task public.")
//
//        val eitherDeveloperEntities = validateDeveloper(accessToken)
//        if (eitherDeveloperEntities.isLeft()) {
//            return eitherDeveloperEntities.getLeft()
//        }
//        modelAndView.view = REDIRECT_VIEW
//
//        val (developer, _) = eitherDeveloperEntities.getRight()
//
//        val task = taskService.getTaskById(taskId)
//        if (task == null) {
//            logger.warn(accessToken, "Task with id '$taskId' not found.")
//
//            val postTaskMessage = "Задача с id '$taskId' не найдена."
//
//            val developerModel = getModel(developer, postTaskMessage)
//            modelAndView.addAllObjects(developerModel.asMap())
//
//            return modelAndView
//        }
//
//        if (isPublic) {
//            val allAdmins = adminService.getAll()
//            task.admins.addAll(allAdmins)
//
//            allAdmins.forEach { admin ->
//                admin.tasks.add(task)
//                adminService.save(admin)
//            }
//
//            taskService.saveTask(task)
//        } else {
//            val allAdmins = adminService.getAll()
//            task.admins.removeAll(allAdmins.toSet())
//
//            allAdmins.forEach { admin -> admin.tasks.remove(task) }
//            adminService.saveAll(allAdmins)
//
//            val allGroups = groupService.getAll()
//            task.groups.removeAll(allGroups)
//
//            allGroups.forEach{group -> group.tasks.remove(task) }
//            groupService.saveAll(allGroups)
//
//            taskService.saveTask(task)
//        }
//
//        task.isPublic = isPublic
//        taskService.saveTask(task)
//
//        logger.info(accessToken, "Task '${task.getFullName()}' was successfully made public.")
//
//        val postTaskMessage = "Задача '${task.getFullName()}' была успешно сделана публичной."
//
//        val developerModel = getModel(developer, postTaskMessage)
//        modelAndView.addAllObjects(developerModel.asMap())
//
//        return modelAndView
//    }
//}