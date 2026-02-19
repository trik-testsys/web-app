package trik.testsys.webapp.notifier

/**
 * @author Viktor Karasev
 * @since 3.12.0
 */
interface IncidentNotifier {
    fun notify(msg: String)
    fun notify(msg: String, e: Exception)
}