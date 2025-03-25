package trik.testsys.webclient.view.impl

/**
 * View for sponsor logos.
 *
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
data class LogosView(
    val hasSponsors: Boolean = false,
    val hasMain: Boolean = false,
    val sponsorLogos: List<LogoView>? = null,
    val mainLogo: LogoView? = null
) {

    data class LogoView(val path: String, val name: String = path.substringAfterLast('/'))

    class Builder {

        private var sponsorLogos: MutableList<LogoView> = mutableListOf()
        private var mainLogo: LogoView? = null

        fun addSponsorLogo(path: String) = apply { sponsorLogos.add(LogoView(path)) }

        fun addMainLogo(path: String) = apply { mainLogo = LogoView(path) }

        fun build(): LogosView {
            return LogosView(
                hasSponsors = sponsorLogos.isNotEmpty(),
                hasMain = mainLogo != null,
                sponsorLogos,
                mainLogo)
        }
    }

    companion object {

        fun empty() = LogosView()

        fun builder() = Builder()
    }
}