package trik.testsys.webclient.controller.impl.rest

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import trik.testsys.webclient.controller.rest.StudentExportController
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.service.entity.impl.GroupService
import trik.testsys.webclient.service.entity.user.impl.AdminService
import trik.testsys.webclient.service.entity.user.impl.StudentService

/**
 * @author Roman Shishkin
 * @since 2.5.0
 */
@RestController
@RequestMapping("rest/student/export")
class StudentExportControllerImpl(
    private val adminService: AdminService,
    private val groupService: GroupService,
    private val studentService: StudentService
) : StudentExportController {


    @PostMapping("csv")
    override fun exportFromCsvFile(
        @RequestParam(required = true) apiKey: String,
        @RequestParam(required = true) adminId: Long,
        @RequestParam(required = true) groupId: Long,
        @RequestPart(required = true) file: MultipartFile
    ): ResponseEntity<StudentExportController.ResponseData> {
        val admin = adminService.find(adminId) ?: return ResponseEntity.badRequest().body(
            StudentExportController.ResponseData.error("Admin with ID $adminId not found")
        )
        val group = groupService.find(groupId) ?: return ResponseEntity.badRequest().body(
            StudentExportController.ResponseData.error("Group with ID $groupId not found")
        )

        if (group.admin.id != admin.id) {
            return ResponseEntity.badRequest().body(
                StudentExportController.ResponseData.error("Admin with ID $adminId is not the owner of the group with ID $groupId")
            )
        }

        val studentsAdditionalInfo = file.parseCsvFile() ?: return ResponseEntity.badRequest().body(
            StudentExportController.ResponseData.error("File is invalid")
        )
        val students = studentService.generate(studentsAdditionalInfo, group)
        val studentsInfo = students.toStudentsInfo()

        return ResponseEntity.ok(StudentExportController.ResponseData.success(studentsInfo))
    }

    private fun MultipartFile.parseCsvFile(): List<String>? {
        val lines = inputStream.bufferedReader().readLines()
        if (lines.isEmpty()) {
            return null
        }

        val header = lines[0]
        val students = lines.drop(1)

        if (students.isEmpty()) {
            return null
        }

        val fields = header.fields
        return students.map { student ->
            val values = student.fields

            if (fields.size != values.size) return null
            fields.zip(values).joinToString(", ") { (field, value) -> "$field: $value" }
        }
    }

    private val String.fields: List<String>
        get() = split(",").map { it.trim() }

    private fun Student.toStudentInfo(): StudentExportController.StudentInfo {
        return StudentExportController.StudentInfo(id!!, name, additionalInfo, accessToken)
    }

    private fun List<Student>.toStudentsInfo(): StudentExportController.StudentsInfo {
        return StudentExportController.StudentsInfo(
            adminId = first().group.admin.id!!,
            groupId = first().group.id!!,
            students = map { it.toStudentInfo() }
        )
    }
}