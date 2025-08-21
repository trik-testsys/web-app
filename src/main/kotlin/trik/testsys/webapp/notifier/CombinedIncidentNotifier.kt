package trik.testsys.webapp.notifier

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

/**
 * @author Viktor Karasev
 * @since %CURRENT_VERSION%
 */
@Component
class CombinedIncidentNotifier(
    @Value("\${spring.application.name}")
    private val appName: String,
    context: ApplicationContext
): IncidentNotifier {

    private val notifiers = context.getBeansOfType(IncidentNotifier::class.java).also {
        it.remove("combinedIncidentNotifier")
    }

    override fun notify(msg: String) {
        notifiers.values.forEach { it.notify("[ $appName ] \n\n $msg") }
    }

    override fun notify(msg: String, e: Exception) {
        notifiers.values.forEach { it.notify("[ $appName ] \n\n $msg", e) }
    }
}
