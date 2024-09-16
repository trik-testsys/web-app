package trik.testsys.webclient.service.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.user.AbstractUserService
import trik.testsys.core.utils.marker.TrikService
import trik.testsys.webclient.entity.impl.Viewer
import trik.testsys.webclient.entity.impl.WebUser
import trik.testsys.webclient.repository.impl.ViewerRepository
import java.security.MessageDigest
import java.util.*

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Service
class ViewerService : AbstractUserService<Viewer, ViewerRepository>(), TrikService {

    fun getByWebUser(webUser: WebUser): Viewer? {
        return repository.findByWebUser(webUser)
    }

    fun save(webUser: WebUser): Viewer {
        val adminRegToken = generateAdminRegToken()
        val viewer = Viewer(webUser, adminRegToken)
        viewer.adminRegToken = adminRegToken

        return repository.save(viewer)
    }

    fun getByAdminRegToken(adminRegToken: String): Viewer? {
        return repository.findByAdminRegToken(adminRegToken)
    }

    private fun generateAdminRegToken(): String {
        val saltedWord = "" + Date().time + Random(Date().time).nextInt()
        val md = MessageDigest.getInstance(HASHING_ALGORITHM)
        val digest = md.digest(saltedWord.toByteArray())

        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    companion object {
        const val HASHING_ALGORITHM = "SHA-1"
    }
}