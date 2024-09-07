package trik.testsys.core.utils.l10n.localizer

import trik.testsys.core.utils.l10n.L10nCode


/**
 * Contract for implementation of localizer.
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface Localizer {

    /**
     * Gets a localized message by [l10nCode].
     */
    fun localize(l10nCode: L10nCode): String
}