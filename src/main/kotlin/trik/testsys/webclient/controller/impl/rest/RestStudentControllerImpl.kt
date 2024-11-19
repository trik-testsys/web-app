package trik.testsys.webclient.controller.impl.rest

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.controller.rest.RestStudentController
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.service.FileManager
import trik.testsys.webclient.service.entity.impl.GroupService
import trik.testsys.webclient.service.entity.impl.SolutionService
import trik.testsys.webclient.service.entity.user.impl.StudentService
import trik.testsys.webclient.service.entity.user.impl.StudentService.Companion.getBestSolutionFor
import trik.testsys.webclient.service.token.access.AccessTokenGenerator
import java.util.UUID

@RestController
@RequestMapping("rest/student")
class RestStudentControllerImpl(
    @Value("\${lektorium.groupRegToken}") private val groupRegToken: AccessToken,

    private val studentService: StudentService,
    private val groupService: GroupService,

    @Qualifier("studentAccessTokenGenerator")
    private val studentAccessTokenGenerator: AccessTokenGenerator,

    private val solutionService: SolutionService,
    private val fileManager: FileManager
) : RestStudentController {

    private fun getGroup(): Group {
        return groupService.findByRegToken(groupRegToken)!!
    }

    @GetMapping("register")
    override fun register(
        @RequestParam(required = true) apiKey: String
    ): ResponseEntity<RestStudentController.StudentData> {
        val uuid = UUID.randomUUID().toString()
        val name = "Student $uuid"
        val accessToken = studentAccessTokenGenerator.generate(name)

        val student = Student(name, accessToken).also {
            it.group = getGroup()
        }
        studentService.save(student)

        val responseBody = RestStudentController.StudentData(student.id!!, accessToken)

        return ResponseEntity.ok(responseBody)
    }

    @GetMapping("results")
    override fun getResults(
        @RequestParam(required = true) apiKey: String,
        @RequestParam(required = true) testSysId: Long
    ): ResponseEntity<List<RestStudentController.TrikResult>> {
        val student = studentService.find(testSysId) ?: run {
            return ResponseEntity.notFound().build()
        }

        val contests = getGroup().contests
        val tasks = contests.map { it.tasks }.flatten()
        val results = tasks.map { task ->
            val solutions = solutionService.findByStudentAndTask(student, task).sortedByDescending { it.creationDate }
            val lastSolution = solutions.firstOrNull()
            val bestSolution = student.getBestSolutionFor(task)

            val gradingResult = when (lastSolution?.status) {
                null -> RestStudentController.GradingResult.NO_SUBMISSIONS
                Solution.SolutionStatus.PASSED -> RestStudentController.GradingResult.PASSED
                Solution.SolutionStatus.FAILED, Solution.SolutionStatus.ERROR -> if (bestSolution != null) RestStudentController.GradingResult.PASSED else RestStudentController.GradingResult.FAILED
                Solution.SolutionStatus.IN_PROGRESS, Solution.SolutionStatus.NOT_STARTED -> RestStudentController.GradingResult.QUEUED
            }

            val bestSolutionId = when (gradingResult) {
                RestStudentController.GradingResult.NO_SUBMISSIONS,
                RestStudentController.GradingResult.FAILED,
                RestStudentController.GradingResult.QUEUED -> null

                RestStudentController.GradingResult.PASSED -> bestSolution!!.id
            }

            val trikTask = RestStudentController.TrikTask(task.id!!, task.name, task.contests.first().id!!, task.contests.first().name)
            val submission = bestSolutionId?.let { RestStudentController.Submission(it) }
            val trikResult = RestStudentController.TrikResult(trikTask, gradingResult, submission)

            trikResult
        }

        return ResponseEntity.ok(results)
    }

    @GetMapping("submission")
    override fun loadSubmission(
        @RequestParam(required = true) apiKey: String,
        @RequestParam(required = true) submissionId: Long
    ): ResponseEntity<ByteArray> {
        val solution = solutionService.find(submissionId) ?: run {
            return ResponseEntity.notFound().build()
        }
        val solutionFile = fileManager.getSolutionFile(solution) ?: run {
            return ResponseEntity.internalServerError().build()
        }
        val bytes = solutionFile.readBytes()

        val responseEntity = ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"${solutionFile.name}\"")
            .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header("Content-Transfer-Encoding", "binary")
            .header("Content-Length", bytes.size.toString())
            .body(bytes)

        return responseEntity
    }
}