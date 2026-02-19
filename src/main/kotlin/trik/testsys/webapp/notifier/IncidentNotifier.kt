package trik.testsys.webapp.notifier

/**
 * @author Viktor Karasev
 * @since %CURRENT_VERSION%
 */
interface IncidentNotifier {
    fun notify(msg: String)
    fun notify(msg: String, e: Exception)
}