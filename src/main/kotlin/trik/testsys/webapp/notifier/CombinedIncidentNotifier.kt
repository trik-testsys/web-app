package trik.testsys.webapp.notifier

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

/**
 * @author Viktor Karasev
 * @since %CURRENT_VERSION%
 */
@Service
class CombinedIncidentNotifier(
    context: ApplicationContext
): IncidentNotifier {

    private val notifiers = context.getBeansOfType(IncidentNotifier::class.java).also {
        it.remove("combinedIncidentNotifier")
    }

    override fun notify(msg: String) {
        notifiers.values.forEach { it.notify(msg) }
    }

    override fun notify(msg: String, e: Exception) {
        notifiers.values.forEach { it.notify(msg, e) }
    }
}
