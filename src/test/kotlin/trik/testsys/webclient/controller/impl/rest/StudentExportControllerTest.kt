package trik.testsys.webclient.controller.impl.rest

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile
import trik.testsys.webclient.controller.rest.StudentExportController
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.entity.user.impl.Viewer
import trik.testsys.webclient.service.entity.impl.GroupService
import trik.testsys.webclient.service.entity.user.impl.AdminService
import trik.testsys.webclient.service.entity.user.impl.StudentService
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
internal class StudentExportControllerTest {

    private val adminService = mock<AdminService>()
    private val groupService = mock<GroupService>()
    private val studentService = mock<StudentService>()

    private val studentExportController = StudentExportControllerImpl(adminService, groupService, studentService)

    @Nested
    inner class `exportFromCsvFile tests` {

        //region Parameters
        private val apiKey = "apiKey"
        private val adminId = 1L
        private val groupId = 1L

        private val admin = admin()
        private val group = group(admin = admin)
        //endregion

        @Test
        fun `admin does not exist by adminId - return bad request`() {
            //region Mocking
            whenever(adminService.find(adminId)) doReturn null
            //endregion

            //region Asserting
            val result = studentExportController.exportFromCsvFile(apiKey, adminId, groupId, multipartFile())
            result.assertFailure(
                expectedHttpStatus = HttpStatus.BAD_REQUEST,
                expectedMessage = "Admin with ID $adminId not found",
            )
            //endregion
        }

        @Test
        fun `admin exist, but group does not exist by groupId - return bad request`() {
            //region Mocking
            whenever(adminService.find(adminId)) doReturn admin
            whenever(groupService.find(groupId)) doReturn null
            //endregion

            //region Asserting
            val result = studentExportController.exportFromCsvFile(apiKey, adminId, groupId, multipartFile())
            result.assertFailure(
                expectedHttpStatus = HttpStatus.BAD_REQUEST,
                expectedMessage = "Group with ID $groupId not found",
            )
            //endregion
        }

        @Test
        fun `admin and group exists, but admin is not owner of group - return bad request`() {
            //region Mocking
            whenever(adminService.find(adminId)) doReturn admin

            val admin2 = admin(2L, "admin2", "accessToken2")
            val group2 = group(admin = admin2)
            whenever(groupService.find(groupId)) doReturn group2
            //endregion

            //region Asserting
            val result = studentExportController.exportFromCsvFile(apiKey, adminId, groupId, multipartFile())
            result.assertFailure(
                expectedHttpStatus = HttpStatus.BAD_REQUEST,
                expectedMessage = "Admin with ID $adminId is not the owner of the group with ID $groupId",
            )
            //endregion
        }

        @Test
        fun `admin and group exists, file is empty - return bad request`() {
            //region Mocking
            whenever(adminService.find(adminId)) doReturn admin
            whenever(groupService.find(groupId)) doReturn group

            val file = multipartFile(
                content = ""
            )
            //endregion

            //region Asserting
            val result = studentExportController.exportFromCsvFile(apiKey, adminId, groupId, file)
            result.assertFailure(
                expectedHttpStatus = HttpStatus.BAD_REQUEST,
                expectedMessage = "File is invalid",
            )
            //endregion
        }

        @Test
        fun `admin and group exists, file contains only header - return bad request`() {
            //region Mocking
            whenever(adminService.find(adminId)) doReturn admin
            whenever(groupService.find(groupId)) doReturn group

            val file = multipartFile(
                content = "Name, Surname, Age"
            )
            //endregion

            //region Asserting
            val result = studentExportController.exportFromCsvFile(apiKey, adminId, groupId, file)
            result.assertFailure(
                expectedHttpStatus = HttpStatus.BAD_REQUEST,
                expectedMessage = "File is invalid",
            )
            //endregion
        }

        @Test
        fun `admin and group exists, file contains header and invalid rows - return bad request`() {
            //region Mocking
            whenever(adminService.find(adminId)) doReturn admin
            whenever(groupService.find(groupId)) doReturn group

            val file = multipartFile(
                content = "Name, Surname, Age\n" +
                          "John, Doe\n" +
                          "Jane, Doe, 25, 123"
            )
            //endregion

            //region Asserting
            val result = studentExportController.exportFromCsvFile(apiKey, adminId, groupId, file)
            result.assertFailure(
                expectedHttpStatus = HttpStatus.BAD_REQUEST,
                expectedMessage = "File is invalid",
            )
            //endregion
        }

        @Test
        fun `admin and group exists, file contains header and valid rows - return bad request`() {
            //region Mocking
            whenever(adminService.find(adminId)) doReturn admin
            whenever(groupService.find(groupId)) doReturn group

            val file = multipartFile(
                content = "Name, Surname, Age\n" +
                        "John, Doe, 25\n" +
                        "Jane, Doe, 23"
            )
            val student1 = Student("st-1", "accessToken1").also {
                it.id = 1L
                it.additionalInfo = "Name: John, Surname: Doe, Age: 25"
                it.group = group
            }
            val student2 = Student("st-2", "accessToke2").also {
                it.id = 2L
                it.additionalInfo = "Name: Jane, Surname: Doe, Age: 23"
                it.group = group
            }
            val students = listOf(student1, student2)

            whenever(studentService.generate(any<List<String>>(), any())) doReturn students
            //endregion

            //region Asserting
            val result = studentExportController.exportFromCsvFile(apiKey, adminId, groupId, file)
            val expectedStudentsInfo = StudentExportController.StudentsInfo(
                adminId = admin.id!!,
                groupId = group.id!!,
                students = students.map {
                    StudentExportController.StudentInfo(
                        id = it.id!!,
                        name = it.name,
                        additionalInfo = it.additionalInfo,
                        accessToken = it.accessToken
                    )
                }
            )
            result.assertSuccess(
                expectedHttpStatus = HttpStatus.OK,
                expectedStudentsInfo = expectedStudentsInfo
            )
            //endregion
        }

        private fun ResponseEntity<StudentExportController.ResponseData>.assert(
            expectedHttpStatus: HttpStatus,
            expectedMessage: String,
            expectedDataStatus: StudentExportController.ResponseData.Status,
            expectedStudentsInfo: StudentExportController.StudentsInfo?
        ) {
            assertEquals(expectedHttpStatus, statusCode)

            assertNotNull(body)
            assertEquals(expectedMessage, body!!.message)
            assertEquals(expectedDataStatus, body!!.status)
            assertEquals(expectedStudentsInfo, body!!.studentsInfo)
        }

        private fun ResponseEntity<StudentExportController.ResponseData>.assertFailure(
            expectedHttpStatus: HttpStatus,
            expectedMessage: String
        ) = assert(
            expectedHttpStatus = expectedHttpStatus,
            expectedMessage = expectedMessage,
            expectedDataStatus = StudentExportController.ResponseData.Status.FAILURE,
            expectedStudentsInfo = null
        )

        private fun ResponseEntity<StudentExportController.ResponseData>.assertSuccess(
            expectedHttpStatus: HttpStatus,
            expectedStudentsInfo: StudentExportController.StudentsInfo
        ) = assert(
            expectedHttpStatus = expectedHttpStatus,
            expectedMessage = "Students have been successfully exported",
            expectedDataStatus = StudentExportController.ResponseData.Status.SUCCESS,
            expectedStudentsInfo = expectedStudentsInfo
        )
    }

    companion object {

        private fun admin(
            id: Long = 1L,
            name: String = "admin",
            accessToken: String = "accessToken",
            viewer: Viewer = Viewer("viewer", "accessToken2", "regToken")
        ) = Admin(name, accessToken).also {
            it.id = id
            it.viewer = viewer
        }

        private fun group(
            id: Long = 10L,
            name: String = "group",
            regToken: String = "regToken2",
            admin: Admin = admin()
        ) = Group(name, regToken).also {
            it.id = id
            it.admin = admin
        }

        private fun multipartFile(
            content: String = ""
        ) = mock<MultipartFile> {
            val inputStream = ByteArrayInputStream(content.toByteArray())
            on { this.inputStream } doReturn inputStream
        }
    }
}