package trik.testsys.webclient.service

import trik.testsys.webclient.view.impl.LogosView

/**
 * Service for logos management.
 *
 * @author Roman Shishkin
 * @since 2.6.5
 */
interface LogoService {

    fun getLogos(): LogosView
}