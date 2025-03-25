package trik.testsys.webclient.service

import trik.testsys.webclient.view.impl.LogosView

/**
 * Service for logos management.
 *
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface LogoService {

    fun getLogos(): LogosView
}