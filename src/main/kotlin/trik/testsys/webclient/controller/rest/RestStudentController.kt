package trik.testsys.webclient.controller.rest

import org.springframework.http.ResponseEntity
import trik.testsys.core.controller.TrikRestController


interface RestStudentController : TrikRestController {

    fun register(apiKey: String): ResponseEntity<StudentData>

    fun getResults(apiKey: String, userData: StudentData): ResponseEntity<List<TrikResult>>

    fun loadSubmission(apiKey: String, submissionId: Long): ResponseEntity<ByteArray>

    data class StudentData(
        val testSysId: Long,
        val testSysKey: String
    )

    data class TrikResult(
        val trikTask: TrikTask,
        val gradingResult: GradingResult,
        val submission: Submission?
    )

    data class TrikTask(
        val id: Long,
        val name: String,
        val contestId: Long,
        val contestName: String
    )

    enum class GradingResult {
        PASSED, FAILED, QUEUED, NO_SUBMISSIONS
    }

    data class Submission(
        val submissionId: Long
    )
}