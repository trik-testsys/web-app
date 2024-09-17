import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.InjectMocks
import org.mockito.Mockito.*
import trik.testsys.webclient.entity.impl.user.Admin
import trik.testsys.webclient.entity.impl.user.WebUser
import trik.testsys.webclient.service.impl.user.AdminService

class AdminServiceTest {

    @InjectMocks
    private val adminService = mock(AdminService::class.java)

    @Test
    fun test() {
        val a = 1
        val b = 2
        val c = a + b
        assertEquals(3, c)
    }

    @Test
    fun test2() {
        val webUser = WebUser("username", "accessToken")
        val admin = Admin(webUser)

        doReturn(admin).`when`(adminService).save(admin)

        adminService.save(admin)
        verify(adminService, times(1)).save(admin)
    }
}