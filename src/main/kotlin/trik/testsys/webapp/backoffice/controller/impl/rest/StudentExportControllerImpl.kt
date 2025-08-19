package trik.testsys.webapp.backoffice.controller.impl.rest

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.impl.UserServiceImpl

/**
 * Export endpoints for viewer-related data.
 */
@RestController
@RequestMapping("/rest/export")
class StudentExportControllerImpl(
    private val userService: UserServiceImpl
) {

    /**
     * Returns a CSV of all students belonging to admins managed by the given viewer.
     * For MVP, emits only headers and one demo row if data not yet modeled.
     */
    @GetMapping("/admin-students")
    fun exportAdminStudents(): ResponseEntity<ByteArray> {
        // TODO: Replace with real data fetch when Student and Group are enabled
        val header = "admin_id,admin_name,student_id,student_name,group_id,group_name,verdict"
        val demo = "0,DUMMY,0,DUMMY,0,DUMMY,UNKNOWN"
        val csv = (header + "\n" + demo).toByteArray()

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=admin_students.csv")
            .contentType(MediaType.TEXT_PLAIN)
            .body(csv)
    }
}
