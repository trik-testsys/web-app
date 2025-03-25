package trik.testsys.webclient.service.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import trik.testsys.webclient.service.LogoService
import trik.testsys.webclient.view.impl.LogosView
import java.io.File

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Service
class LogoServiceImpl(
    @Value("\${path.logos.sponsor}") private val sponsorLogosPath: String,
    @Value("\${path.logos.main}") private val mainLogoFilePath: String,
) : LogoService {

    private val sponsorLogosDir = File(sponsorLogosPath)
    private val mainLogo = File(mainLogoFilePath)

    init {
        if (!sponsorLogosDir.exists() || !sponsorLogosDir.isDirectory) {
            logger.warn("Sponsor logos directory not found: $sponsorLogosPath. Creating...")
            sponsorLogosDir.mkdirs()
        }

        if (!mainLogo.exists() || !mainLogo.isFile) {
            logger.warn("Main logo file not found: $mainLogoFilePath")
        }
    }

    override fun getLogos(): LogosView {
        val builder = LogosView.builder()

        sponsorLogosDir.listFiles()
            ?.filter { it.isFile }
            ?.forEach { builder.addSponsorLogo(it.path) }

        mainLogo.takeIf { it.exists() }?.let { builder.addMainLogo(it.path) }

        return builder.build()
    }

    companion object {

        private val logger = LoggerFactory.getLogger(LogoServiceImpl::class.java)
    }
}