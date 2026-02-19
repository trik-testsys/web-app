package trik.testsys.webapp.grading

import org.springframework.stereotype.Component
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.times

/**
 * @author Vyacheslav Buchin
 * @since 3.12.0
 */
interface GraderConfiguration {
    val statusResponseTimeout: Duration
    val nodePollingInterval: Duration
    val resendHangingSubmissionsInterval: Duration
    val hangTimeout: Duration
}

/**
 * @author Vyacheslav Buchin
 * @since 3.12.0
 */
@Component
object DefaultGraderConfiguration : GraderConfiguration {
    override val statusResponseTimeout = 2.seconds
    override val nodePollingInterval = 1.seconds
    override val resendHangingSubmissionsInterval = 1.minutes
    override val hangTimeout = 2 * 5.minutes
}