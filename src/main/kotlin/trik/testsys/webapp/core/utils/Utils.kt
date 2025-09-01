package trik.testsys.webapp.core.utils

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
inline fun <T : Any> Boolean.ifTrue(block: () -> T?): T? {
    return if (this) block.invoke()
    else null
}