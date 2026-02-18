package trik.testsys.webapp.backoffice.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

@Service
class SponsorshipService(
    @Value("\${trik.testsys.paths.sponsorship}") private val sponsorshipDirPath: String
) {

    private val imageExtensions = setOf("png", "jpg", "jpeg", "svg", "gif", "webp")

    fun getImageNames(): List<String> {
        val dir = File(sponsorshipDirPath)
        if (!dir.isDirectory) return emptyList()
        return dir.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in imageExtensions }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
    }
}
