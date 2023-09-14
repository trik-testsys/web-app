package trik.testsys.webclient.model

import trik.testsys.webclient.entity.Admin
import trik.testsys.webclient.entity.Task
import trik.testsys.webclient.util.exception.TrikException

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
class DeveloperModel private constructor(
    val accessToken: String,
    val username: String,
    val postTaskMessage: String?,
    val tasks: Collection<Task>,
    val admins: Collection<Admin>
) : TrikModel {

    class Builder internal constructor() {

        private var accessToken: String? = null
        private var username: String? = null
        private var postTaskMessage: String? = null
        private var tasks: Collection<Task>? = null
        private var admins: Collection<Admin>? = null

        fun accessToken(accessToken: String) = apply { this.accessToken = accessToken }

        fun username(username: String) = apply { this.username = username }

        fun postTaskMessage(postTaskMessage: String) = apply { this.postTaskMessage = postTaskMessage }

        fun tasks(tasks: Collection<Task>) = apply { this.tasks = tasks }

        fun admins(admins: Collection<Admin>) = apply { this.admins = admins }

        fun build() = DeveloperModel(
            accessToken ?: throw TrikException(String.format(PARAMETER_ERROR, DeveloperModel::accessToken.name)),
            username ?: throw TrikException(String.format(PARAMETER_ERROR, DeveloperModel::username.name)),
            postTaskMessage,
            tasks ?: throw TrikException(String.format(PARAMETER_ERROR, DeveloperModel::tasks.name)),
            admins ?: throw TrikException(String.format(PARAMETER_ERROR, DeveloperModel::admins.name))
        )

    }

    override fun asMap(): Map<String, Any?> {
        val argsMap = mutableMapOf<String, Any?>()

        argsMap[this::accessToken.name] = accessToken
        argsMap[this::username.name] = username
        argsMap[this::postTaskMessage.name] = postTaskMessage
        argsMap[this::tasks.name] = tasks.sortedBy { it.id }
        argsMap[this::admins.name] = admins.sortedBy { it.id }

        return argsMap
    }

    companion object {
        private const val PARAMETER_ERROR = "Parameter '%s' must be specified."
    }
}
