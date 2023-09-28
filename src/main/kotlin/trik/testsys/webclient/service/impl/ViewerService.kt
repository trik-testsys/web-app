package trik.testsys.webclient.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.Viewer
import trik.testsys.webclient.entity.WebUser
import trik.testsys.webclient.repository.ViewerRepository
import trik.testsys.webclient.service.TrikService
import java.security.MessageDigest
import java.util.*

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Service
class ViewerService @Autowired constructor(
    private val viewerRepository: ViewerRepository
) : TrikService {

    fun getByWebUser(webUser: WebUser): Viewer? {
        return viewerRepository.findViewerByWebUser(webUser)
    }

    fun save(viewer: Viewer): Viewer {
        return viewerRepository.save(viewer)
    }

    fun save(webUser: WebUser): Viewer {
        val viewer = Viewer(webUser)
        val adminRegToken = generateAdminRegToken(webUser.accessToken)
        viewer.adminRegToken = adminRegToken

        return viewerRepository.save(viewer)
    }

    fun getByAdminRegToken(adminRegToken: String): Viewer? {
        return viewerRepository.findByAdminRegToken(adminRegToken)
    }

    private fun generateAdminRegToken(word: String): String {
        val saltedWord = word + Date().time + Random(Date().time).nextInt()
        val md = MessageDigest.getInstance(HASHING_ALGORITHM)
        val digest = md.digest(saltedWord.toByteArray())

        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    companion object {
        const val HASHING_ALGORITHM = "SHA-1"
    }
}