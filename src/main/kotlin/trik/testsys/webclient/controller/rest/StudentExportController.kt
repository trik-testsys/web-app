package trik.testsys.webclient.controller.rest

import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile
import trik.testsys.core.controller.TrikRestController

/**
 * Controller for exporting students from files.
 *
 * @author Roman Shishkin
 * @since 2.5.0
 */
interface StudentExportController : TrikRestController {

    /**
     * Generates and registers a new student in the system.
     *
     * @param apiKey API key for the request
     * @param adminId ID of the admin who is registering the student
     * @param groupId ID of the group to which the student will be added
     * @param file File with student data
     *
     * @return Student data
     *
     * @author Roman Shishkin
     * @since 2.5.0
     */
    fun exportFromCsvFile(
        apiKey: String,
        adminId: Long,
        groupId: Long,
        file: MultipartFile
    ): ResponseEntity<ResponseData>

    data class ResponseData(
        val message: String,
        val status: Status,
        val studentsInfo: StudentsInfo? = null
    ) {

        enum class Status {
            SUCCESS, FAILURE
        }

        companion object {

            fun success(studentsInfo: StudentsInfo): ResponseData {
                return ResponseData("Students have been successfully exported", Status.SUCCESS, studentsInfo)
            }

            fun error(message: String): ResponseData {
                return ResponseData(message, Status.FAILURE)
            }
        }
    }

    data class StudentsInfo(
        val adminId: Long,
        val groupId: Long,
        val students: List<StudentInfo>
    )

    data class StudentInfo(
        val id: Long,
        val name: String,
        val additionalInfo: String,
        val accessToken: String
    )
}