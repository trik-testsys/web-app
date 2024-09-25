package trik.testsys.webclient.controller.impl.user

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import trik.testsys.webclient.controller.user.UserController
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.service.entity.user.impl.ViewerService
import trik.testsys.webclient.service.security.login.impl.LoginData
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.view.ViewerView
import java.util.*


@Controller
@RequestMapping(ViewerController.VIEWER_PATH)
class ViewerController(
    loginData: LoginData
) : UserController<Viewer, ViewerView, ViewerService>(loginData) {

    override val MAIN_PATH = VIEWER_PATH

    override val MAIN_PAGE = VIEWER_PAGE

    override fun Viewer.toView(timeZone: TimeZone) = ViewerView(
        id = this.id,
        name = this.name,
        accessToken = this.accessToken,
        lastLoginDate = this.lastLoginDate.atTimeZone(timeZone),
        creationDate = this.creationDate?.atTimeZone(timeZone),
        adminRegToken = this.adminRegToken,
        additionalInfo = this.additionalInfo
    )

    companion object {

        const val VIEWER_PATH = "/viewer"

        const val VIEWER_PAGE = "viewer"
    }
}

//    private fun generateAdminsResult(admins: Collection<Admin>): Map<Long, Table> {
//        val adminsResult = mutableMapOf<Long, Table>()
//        admins.forEach { admin ->
//            val adminResult = generateAdminResult(admin)
//            adminsResult[admin.id!!] = adminResult
//        }
//
//        return adminsResult
//    }
//
//    private fun generateAdminResult(admin: Admin): Table {
//        val tasks = admin.tasks.toList().sortedBy { it.id }
//
//        val students = mutableSetOf<Student>()
//        val groups = admin.groups
//        groups.forEach { group ->
//            students.addAll(group.students)
//        }
//
//        val table = generateTable(tasks, students.toList().sortedBy { it.id })
//
//        return table
//    }
//
//    private fun generateGroupsResult(groups: Collection<Group>): Map<Long, Table> {
//        val groupsResult = mutableMapOf<Long, Table>()
//        groups.forEach { group ->
//            val groupResult = generateGroupResult(group)
//            groupsResult[group.id!!] = groupResult
//        }
//
//        return groupsResult
//    }
//
//    private fun generateGroupResult(group: Group): Table {
//        val tasks = group.tasks.toList().sortedBy { it.id }
//        val students = group.students.toList().sortedBy { it.id }
//        val table = generateTable(tasks, students)
//
//        return table
//    }
//
//    private fun generateTable(tasks: List<Task>, students: List<Student>): Table {
//        val header = mutableListOf<String>()
//        tasks.forEach { task ->
//            val taskNameWithId = "${task.id}: ${task.name}"
//            header.add(taskNameWithId)
//        }
//
//        val rows = mutableListOf<TableRow>()
//        students.forEach { student ->
//            val tasksInfo = mutableListOf<Long>()
//            tasks.forEach { task ->
//                val failedSolution =
//                    student.solutions.find { it.task.id == task.id && (it.status == Solution.Status.FAILED || it.status == Solution.Status.ERROR) }
//                val passedSolution =
//                    student.solutions.find { it.task == task && it.status == Solution.Status.PASSED }
//                val inProgressSolution =
//                    student.solutions.find { it.task == task && (it.status == Solution.Status.IN_PROGRESS || it.status == Solution.Status.NOT_STARTED) }
//
//                if (failedSolution != null) {
//                    tasksInfo.add(0)
//                } else if (passedSolution != null) {
//                    tasksInfo.add(1)
//                } else if (inProgressSolution != null) {
//                    tasksInfo.add(2)
//                } else {
//                    tasksInfo.add(-1)
//                }
//            }
//            rows.add(TableRow(student.id!!, student.webUser.username, tasksInfo))
//        }
//
//        return Table(header, rows)
//    }
//
//    @RequestMapping("/results/download")
//    fun getAllResults(
//        @RequestParam accessToken: String,
//        modelAndView: ModelAndView
//    ): Any {
//        logger.info(accessToken, "Client trying to get all results.")
//
//        val eitherViewer = validateViewer(accessToken)
//        if (eitherViewer.isLeft()) {
//            return eitherViewer.getLeft()
//        }
//        val viewer = eitherViewer.getRight()
//        val admins = viewer.admins.sortedBy { it.id }
//        logger.info(accessToken, "Admins (${admins.size}): $admins")
//
//        val groups = admins.flatMap { it.groups }.sortedBy { it.id }
//        logger.info(accessToken, "Groups (${groups.size}): $groups")
//
//        val students = groups.flatMap { it.students }.sortedBy { it.id }.filter { solutionService.countByStudent(it) > 0 }
//        logger.info(accessToken, "Students (${students.size}): $students")
//
//        val tasks = students.flatMap { it.solutions }.map { it.task }.distinct().sortedBy { it.id }
//
//        val csvDelimiter = ";"
//        val tasksString = tasks.joinToString(csvDelimiter) { String.format("\"%d: %s\"", it.id, it.name) } + ";"
//
//        val csvHeader =
//            "\"student_id\";\"student_name\";\"district_id\";\"district_name\";\"school_id\";\"school_name\";\"group_id\";\"group_name\";$tasksString\"best_score\"\n"
//
//        val studentsResults = mutableMapOf<Long, List<Long>>()
//        students.forEach { student ->
//            val studentScores = mutableListOf<Long>()
//            tasks.forEach { task ->
//                val bestSolution = solutionService.getBestSolutionByTaskAndStudent(task, student)
//                val score = bestSolution?.score ?: 0
//
//                studentScores.add(score)
//            }
//            val maxScore = studentScores.maxOrNull() ?: 0
//            studentScores.add(maxScore)
//            studentsResults[student.id!!] = studentScores
//        }
//
//        val csvBody = mutableListOf<String>()
//        students.forEach { student ->
//            val studentScores = studentsResults[student.id!!]!!
//            val studentScoresString = studentScores.joinToString(csvDelimiter)
//
//            val webUser = student.webUser
//            val group = student.group
//            val admin = group.admin
//
//            val studentInfo = "\"${student.id}\";\"${webUser.username}\";\"${viewer.id}\";\"${viewer.webUser.username}\";\"${admin.id}\";\"${admin.webUser.username}\";\"${group.id}\";\"${group.name}\";$studentScoresString"
//
//            csvBody.add(studentInfo)
//        }
//
//        val csv = csvHeader + csvBody.joinToString("\n")
//        val bytes = csv.toByteArray()
//
//        val headers = HttpHeaders()
//        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
//        headers.contentDisposition = ContentDisposition.builder("attachment")
//            .filename("results-${LocalDateTime.now(UTC).plusHours(3)}.csv")
//            .build()
//
//        headers.acceptLanguage = Locale.LanguageRange.parse("ru-RU, en-US")
//        headers.acceptCharset = listOf(Charsets.UTF_8, Charsets.ISO_8859_1, Charsets.US_ASCII)
//
//        headers.acceptCharset.add(Charset.forName("windows-1251"))
//        headers.acceptCharset.add(Charset.forName("windows-1252"))
//        headers.acceptCharset.add(Charset.forName("windows-1254"))
//        headers.acceptCharset.add(Charset.forName("windows-1257"))
//        headers.acceptCharset.add(Charset.forName("windows-1258"))
//        headers.acceptCharset.add(Charset.forName("windows-874"))
//        headers.acceptCharset.add(Charset.forName("windows-949"))
//        headers.acceptCharset.add(Charset.forName("windows-950"))
//        headers.acceptCharset.add(Charset.forName("ANSI_X3.4-1968"))
//
//        val responseEntity = ResponseEntity.ok()
//            .headers(headers)
//            .body(bytes)
//
//        return responseEntity
//    }
//
//    data class Table(
//        val header: List<String>,
//        val rows: List<TableRow>
//    )
//
//    data class TableRow(
//        val username: String,
//        val tasksInfo: List<Long>
//    ) {
//        constructor(
//            studentId: Long, studentName: String,
//            tasksInfo: List<Long>
//        ) : this(
//            "$studentId: $studentName",
//            tasksInfo
//        )
//    }
//
//    private fun getModel(
//        viewer: Viewer,
//        groupsResult: Map<Long, Table>,
//        adminsResult: Map<Long, Table>
//    ): ViewerModel {
//        val webUser = viewer.webUser
//
//        val admins = viewer.admins.sortedBy { it.id }
//        val groups = admins.flatMap { it.groups }.sortedBy { it.id }
//
//        val viewerModel = ViewerModel.Builder()
//            .accessToken(webUser.accessToken)
//            .adminRegToken(viewer.adminRegToken)
//            .username(webUser.username)
//            .additionalInfo(webUser.additionalInfo)
//            .admins(admins)
//            .groups(groups)
//            .groupsResult(groupsResult)
//            .adminsResult(adminsResult)
//            .lastLoginDate(webUser.lastLoginDate)
//            .build()
//
//        return viewerModel
//    }