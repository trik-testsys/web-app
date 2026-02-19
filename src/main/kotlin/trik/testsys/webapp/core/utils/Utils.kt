package trik.testsys.webapp.core.utils

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
inline fun <T : Any> Boolean.ifTrue(block: () -> T?): T? {
    return if (this) block.invoke()
    else null
}