//package trik.testsys.webapp.backoffice.controller.impl.user.judge
//
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.http.ResponseEntity
//import org.springframework.stereotype.Controller
//import org.springframework.ui.Model
//import org.springframework.web.bind.annotation.CookieValue
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.ModelAttribute
//import org.springframework.web.bind.annotation.PathVariable
//import org.springframework.web.bind.annotation.PostMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RequestParam
//import org.springframework.web.servlet.mvc.support.RedirectAttributes
//import trik.testsys.backoffice.controller.impl.main.LoginController.Companion.LOGIN_PATH
//import trik.testsys.backoffice.controller.impl.user.judge.JudgeMainController.Companion.JUDGE_PAGE
//import trik.testsys.backoffice.controller.impl.user.judge.JudgeStudentController.Companion.STUDENT_PATH
//import trik.testsys.backoffice.controller.impl.user.judge.JudgeStudentsController.Companion.STUDENTS_PAGE
//import trik.testsys.backoffice.controller.impl.user.judge.JudgeStudentsController.Companion.STUDENTS_PATH
//import trik.testsys.backoffice.controller.user.AbstractWebUserController
//import trik.testsys.backoffice.entity.impl.Solution
//import trik.testsys.backoffice.entity.impl.SolutionVerdict
//import trik.testsys.backoffice.entity.user.impl.Judge
//import trik.testsys.backoffice.service.FileManager
//import trik.testsys.backoffice.service.Grader
//import trik.testsys.backoffice.service.entity.impl.SolutionService
//import trik.testsys.backoffice.service.entity.impl.SolutionVerdictService
//import trik.testsys.backoffice.service.entity.user.impl.JudgeService
//import trik.testsys.backoffice.service.entity.user.impl.StudentService
//import trik.testsys.backoffice.service.security.login.impl.LoginData
//import trik.testsys.backoffice.util.addPopupMessage
//import trik.testsys.backoffice.view.impl.*
//import trik.testsys.backoffice.view.impl.TaskTestResultView.Companion.toTaskTestResultView
//
//@Controller
//@RequestMapping(STUDENT_PATH)
//class JudgeStudentController(
//    loginData: LoginData,
//
//    private val studentService: StudentService,
//    private val solutionService: SolutionService,
//    private val solutionVerdictService: SolutionVerdictService,
//
//    private val fileManager: FileManager,
//    private val grader: Grader,
//
//    @Value("\${trik-studio-version}") private val trikStudioVersion: String
//) : AbstractWebUserController<Judge, JudgeView, JudgeService>(loginData) {
//
//    override val mainPath = STUDENTS_PATH
//
//    override val mainPage = STUDENTS_PAGE
//
//    override fun Judge.toView(timeZoneId: String?) = TODO()
//
//    @GetMapping("/{studentId}")
//    fun studentGet(
//        @PathVariable studentId: Long,
//        @ModelAttribute("studentFilter") filter: StudentSolutionFilter,
//        @CookieValue(name = "X-Timezone", defaultValue = "UTC") timezone: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"
//
//        val student = studentService.find(studentId) ?: run {
//            redirectAttributes.addPopupMessage("Участник с ID $studentId не найден")
//            return "redirect:$STUDENTS_PATH"
//        }
//        model.addAttribute(STUDENT_ATTR, student.toView(timezone))
//
//        val verdicts = solutionVerdictService.findByStudent(student)
//        val verdictsTasks = verdicts.map { it.task }
//        model.addAttribute(VERDICTS_ATTR, verdicts.map { it.toView(timezone) }.sortedByDescending { it.creationDate })
//
//        val solutions = student.solutions.asSequence()
//            .filter { filter.taskId?.let { taskId -> it.task.id == taskId } ?: true }
//            .filter { filter.solutionId?.let { solutionId -> it.id == solutionId } ?: true }
//            .map { it.toTaskTestResultView(timezone) }
//            .toList()
//            .sortedByDescending { it.creationDate }
//
//        val tasks = student.solutions
//            .map { it.task }
//            .filter { it !in verdictsTasks }
//            .toSet().sortedBy { it.id }.toList()
//        model.addAttribute(TASKS_ATTR, tasks)
//
//        if (solutions.isEmpty()) {
//            model.addAttribute("message", "По заданному фильтру Решений не найдено")
//            model.addAttribute(SOLUTIONS_ATTR, emptyList<TaskTestResultView>())
//            return STUDENT_PAGE
//        }
//
//        model.addAttribute(SOLUTIONS_ATTR, solutions)
//
//        return STUDENT_PAGE
//    }
//
//    @PostMapping("/rerunSolution/{studentId}")
//    fun rerunStudentSolution(
//        @PathVariable studentId: Long,
//        @RequestParam solutionId: Long,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"
//
//        val student = studentService.find(studentId) ?: run {
//            redirectAttributes.addPopupMessage("Участник с ID $studentId не найден")
//            return "redirect:$STUDENTS_PATH"
//        }
//
//        val solution = solutionService.find(solutionId) ?: run {
//            redirectAttributes.addPopupMessage("Решение с ID $solutionId не найдено")
//            return "redirect:$STUDENT_PATH/$studentId"
//        }
//
//        if (student.solutions.none { it.id == solutionId }) {
//            redirectAttributes.addPopupMessage("Решение с ID $solutionId не принадлежит участнику с ID $studentId")
//            return "redirect:$STUDENT_PATH/$studentId"
//        }
//
//        val file = fileManager.getSolutionFile(solution)!!
//        val newSolution = Solution.qrsSolution(solution.task).also {
//            it.student = solution.student
//            it.additionalInfo = "Перезапуск посылки с ID ${solution.id} Судьей ${webUser.id}: ${webUser.name}"
//        }
//        solutionService.save(newSolution)
//        fileManager.saveSolutionFile(newSolution, file)
//
//        grader.sendToGrade(
//            newSolution,
//            Grader.GradingOptions(true, trikStudioVersion)
//        )
//
//        redirectAttributes.addPopupMessage("Посылка с ID $solutionId перезапущена")
//
//        return "redirect:$STUDENT_PATH/$studentId"
//    }
//
//    @GetMapping("/downloadSolution/{studentId}")
//    fun downloadStudentSolution(
//        @PathVariable studentId: Long,
//        @RequestParam solutionId: Long,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): Any {
//        loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"
//
//        val student = studentService.find(studentId) ?: run {
//            redirectAttributes.addPopupMessage("Участник с ID $studentId не найден")
//            return "redirect:$STUDENTS_PATH"
//        }
//
//        val solution = solutionService.find(solutionId) ?: run {
//            redirectAttributes.addPopupMessage("Решение с ID $solutionId не найдено")
//            return "redirect:$STUDENT_PATH/$studentId"
//        }
//
//        if (student.solutions.none { it.id == solutionId }) {
//            redirectAttributes.addPopupMessage("Решение с ID $solutionId не принадлежит участнику с ID $studentId")
//            return "redirect:$STUDENT_PATH/$studentId"
//        }
//
//        val file = fileManager.getSolutionFile(solution)!!
//
//        val responseEntity = ResponseEntity.ok()
//            .header("Content-Disposition", "attachment; filename=${file.name}")
//            .header("Content-Type", "application/octet-stream")
//            .header("Content-Length", file.length().toString())
//            .body(file.readBytes())
//
//        return responseEntity
//    }
//
//    @GetMapping("/downloadResults/{studentId}")
//    fun downloadStudentResults(
//        @PathVariable studentId: Long,
//        @RequestParam solutionId: Long,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): Any {
//        loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"
//
//        val student = studentService.find(studentId) ?: run {
//            redirectAttributes.addPopupMessage("Участник с ID $studentId не найден")
//            return "redirect:$STUDENTS_PATH"
//        }
//
//        val solution = solutionService.find(solutionId) ?: run {
//            redirectAttributes.addPopupMessage("Решение с ID $solutionId не найдено")
//            return "redirect:$STUDENT_PATH/$studentId"
//        }
//
//        if (student.solutions.none { it.id == solutionId }) {
//            redirectAttributes.addPopupMessage("Решение с ID $solutionId не принадлежит участнику с ID $studentId")
//            return "redirect:$STUDENT_PATH/$studentId"
//        }
//
//        if (solution.status == Solution.SolutionStatus.IN_PROGRESS || solution.status == Solution.SolutionStatus.NOT_STARTED) {
//            redirectAttributes.addPopupMessage("Решение с ID $solutionId еще не проверено")
//            return "redirect:$STUDENT_PATH/$studentId"
//        }
//
//        val file = fileManager.getSolutionResultFilesCompressed(solution)
//
//        val responseEntity = ResponseEntity.ok()
//            .header("Content-Disposition", "attachment; filename=${file.name}")
//            .header("Content-Type", "application/octet-stream")
//            .header("Content-Length", file.length().toString())
//            .body(file.readBytes())
//
//        return responseEntity
//    }
//
//    @PostMapping("/addVerdict/{studentId}")
//    fun addVerdict(
//        @PathVariable studentId: Long,
//        @RequestParam taskId: Long,
//        @RequestParam score: Long,
//        @RequestParam additionalInfo: String,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"
//
//        val student = studentService.find(studentId) ?: run {
//            redirectAttributes.addPopupMessage("Участник с ID $studentId не найден")
//            return "redirect:$STUDENTS_PATH"
//        }
//
//        val task = student.solutions.firstOrNull { it.task.id == taskId }?.task ?: run {
//            redirectAttributes.addPopupMessage("Задание с ID $taskId не найдено у участника с ID $studentId")
//            return "redirect:$STUDENT_PATH/$studentId"
//        }
//
//        val solutionVerdict = solutionVerdictService.findByStudentAndTask(student, task)
//            .firstOrNull()?.also {
//                it.score = score
//                it.additionalInfo = additionalInfo
//            }
//            ?: SolutionVerdict(
//                name = "Вердикт для Участника '${student.id}: ${student.name}' по Заданию '${task.id}: ${task.name}'",
//                judge = webUser,
//                student = student,
//                task = task,
//                contest = task.contests.firstOrNull(),
//                score = score
//            ).also { it.additionalInfo = additionalInfo }
//        solutionVerdictService.save(solutionVerdict)
//
//        redirectAttributes.addPopupMessage("Вердикт добавлен")
//
//        return "redirect:$STUDENT_PATH/$studentId"
//    }
//
//    @PostMapping("/deleteVerdict/{studentId}")
//    fun deleteVerdict(
//        @PathVariable studentId: Long,
//        @RequestParam verdictId: Long,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ): String {
//        val webUser = loginData.validate(redirectAttributes) ?: return "redirect:$LOGIN_PATH"
//
//        val student = studentService.find(studentId) ?: run {
//            redirectAttributes.addPopupMessage("Участник с ID $studentId не найден")
//            return "redirect:$STUDENTS_PATH"
//        }
//
//        val solutionVerdict = solutionVerdictService.find(verdictId) ?: run {
//            redirectAttributes.addPopupMessage("Вердикт с ID $verdictId не найден")
//            return "redirect:$STUDENT_PATH/$studentId"
//        }
//
//        if (solutionVerdict.judge.id != webUser.id) {
//            redirectAttributes.addPopupMessage("Вердикт с ID $verdictId не принадлежит судье с ID ${webUser.id}")
//            return "redirect:$STUDENT_PATH/$studentId"
//        }
//
//        if (solutionVerdict.student.id != studentId) {
//            redirectAttributes.addPopupMessage("Вердикт с ID $verdictId не принадлежит участнику с ID $studentId")
//            return "redirect:$STUDENT_PATH/$studentId"
//        }
//
//        solutionVerdictService.delete(solutionVerdict)
//
//        redirectAttributes.addPopupMessage("Вердикт удален")
//
//        return "redirect:$STUDENT_PATH/$studentId"
//    }
//
//    companion object {
//
//        const val STUDENT_PATH = "$STUDENTS_PATH/student"
//        const val STUDENT_PAGE = "$JUDGE_PAGE/student"
//
//        const val STUDENT_ATTR = "student"
//        const val SOLUTIONS_ATTR = "solutions"
//        const val VERDICTS_ATTR = "verdicts"
//
//        const val TASKS_ATTR = "tasks"
//    }
//}