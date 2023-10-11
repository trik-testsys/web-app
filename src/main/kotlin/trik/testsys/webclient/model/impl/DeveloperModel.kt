package trik.testsys.webclient.model.impl

import trik.testsys.webclient.entity.impl.Admin
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.model.TrikModel
import trik.testsys.webclient.util.exception.impl.TrikIllegalStateException
import java.time.LocalDateTime

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
class DeveloperModel private constructor(
    val accessToken: String,
    val username: String,
    val postTaskMessage: String?,
    val tasks: Collection<Task>,
    val publicTasks: Collection<Task>,
    val admins: Collection<Admin>,
    val additionalInfo: String,
    val lastLoginDate: LocalDateTime?
) : TrikModel {

    class Builder internal constructor() {

        private var accessToken: String? = null
        private var username: String? = null
        private var postTaskMessage: String? = null
        private var tasks: Collection<Task>? = null
        private var publicTasks: Collection<Task>? = null
        private var admins: Collection<Admin>? = null
        private var additionalInfo: String? = null
        private var lastLoginDate: LocalDateTime? = null

        fun accessToken(accessToken: String) = apply { this.accessToken = accessToken }

        fun username(username: String) = apply { this.username = username }

        fun postTaskMessage(postTaskMessage: String?) = apply { this.postTaskMessage = postTaskMessage }

        fun tasks(tasks: Collection<Task>) = apply { this.tasks = tasks }

        fun publicTasks(publicTasks: Collection<Task>) = apply { this.publicTasks = publicTasks }

        fun admins(admins: Collection<Admin>) = apply { this.admins = admins }

        fun additionalInfo(additionalInfo: String?) = apply { this.additionalInfo = additionalInfo }

        fun lastLoginDate(lastLoginDate: LocalDateTime?) = apply { this.lastLoginDate = lastLoginDate }

        fun build() = DeveloperModel(
            accessToken ?: throw TrikIllegalStateException(String.format(PARAMETER_ERROR, DeveloperModel::accessToken.name)),
            username ?: throw TrikIllegalStateException(String.format(PARAMETER_ERROR, DeveloperModel::username.name)),
            postTaskMessage,
            tasks ?: emptySet(),
            publicTasks ?: emptySet(),
            admins ?: throw TrikIllegalStateException(String.format(PARAMETER_ERROR, DeveloperModel::admins.name)),
            additionalInfo ?: "",
            lastLoginDate
        )

    }

    override fun asMap(): Map<String, Any?> {
        val argsMap = mutableMapOf<String, Any?>()

        argsMap[this::accessToken.name] = accessToken
        argsMap[this::username.name] = username
        argsMap[this::postTaskMessage.name] = postTaskMessage
        argsMap[this::tasks.name] = tasks.sortedBy { it.id }
        argsMap[this::publicTasks.name] = publicTasks.sortedBy { it.id }
        argsMap[this::admins.name] = admins.sortedBy { it.id }
        argsMap[this::additionalInfo.name] = additionalInfo
        argsMap[this::lastLoginDate.name] = lastLoginDate

        return argsMap
    }

    companion object {
        private const val PARAMETER_ERROR = "Parameter '%s' must be specified."
    }
}
