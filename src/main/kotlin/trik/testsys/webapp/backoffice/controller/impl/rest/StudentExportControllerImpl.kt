package trik.testsys.webapp.backoffice.controller.impl.rest

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import jakarta.servlet.http.HttpSession
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import trik.testsys.webapp.backoffice.data.entity.impl.User
import trik.testsys.webapp.backoffice.data.service.StudentGroupService
import trik.testsys.webapp.backoffice.data.service.impl.AccessTokenService

/**
 * Export endpoints for viewer-related data.
 */
@RestController
@RequestMapping("/rest/export")
class StudentExportControllerImpl(
    private val accessTokenService: AccessTokenService,
    private val studentGroupService: StudentGroupService
) {

    /**
     * Returns a CSV of all students belonging to admins managed by the given viewer.
     * For MVP, emits only headers and one demo row if data not yet modeled.
     */
    @GetMapping("/admin-students")
    fun exportAdminStudents(session: HttpSession): ResponseEntity<ByteArray> {
        val tokenValue = session.getAttribute("accessToken") as? String
            ?: return ResponseEntity.status(401).build()

        val token = accessTokenService.findByValue(tokenValue)
            ?: return ResponseEntity.status(401).build()

        val viewer = token.user ?: return ResponseEntity.status(401).build()
        if (!viewer.privileges.contains(User.Privilege.VIEWER)) {
            return ResponseEntity.status(403).build()
        }

        val groups = viewer.managedAdmins
            .asSequence()
            .flatMap { it.ownedStudentGroups.asSequence() }
            .distinct()
            .sortedBy { it.id }
            .toList()

        val csv = studentGroupService.generateResultsCsv(groups)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=viewer_results.csv")
            .contentType(MediaType.TEXT_PLAIN)
            .body(csv)
    }
}
