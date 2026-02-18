package trik.testsys.backoffice.data

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import trik.testsys.webapp.backoffice.data.repository.support.SolutionSpecifications

class SolutionSpecificationsTest {

    @Test
    fun `hasStudentPrivilege returns non-null specification`() {
        assertNotNull(SolutionSpecifications.hasStudentPrivilege())
    }

    @Test
    fun `createdBy returns non-null specification`() {
        assertNotNull(SolutionSpecifications.createdBy(1L))
    }

    @Test
    fun `inGroup returns non-null specification`() {
        assertNotNull(SolutionSpecifications.inGroup(1L))
    }

    @Test
    fun `underAdmin returns non-null specification`() {
        assertNotNull(SolutionSpecifications.underAdmin(1L))
    }

    @Test
    fun `underViewer returns non-null specification`() {
        assertNotNull(SolutionSpecifications.underViewer(1L))
    }

    @Test
    fun `createdAfter returns non-null specification`() {
        assertNotNull(SolutionSpecifications.createdAfter(java.time.Instant.now()))
    }

    @Test
    fun `createdBefore returns non-null specification`() {
        assertNotNull(SolutionSpecifications.createdBefore(java.time.Instant.now()))
    }
}
