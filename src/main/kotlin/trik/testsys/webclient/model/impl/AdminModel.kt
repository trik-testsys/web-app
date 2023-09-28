package trik.testsys.webclient.model.impl

import trik.testsys.webclient.entity.Group
import trik.testsys.webclient.entity.Label
import trik.testsys.webclient.entity.Task
import trik.testsys.webclient.entity.Viewer
import trik.testsys.webclient.model.TrikModel
import trik.testsys.webclient.util.exception.TrikException

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
class AdminModel private constructor(
    val accessToken: String,
    val username: String,
    val groups: Collection<Group>,
    val tasks: Collection<Task>,
    val labels: Collection<Label>
) : TrikModel {

    class Builder internal constructor() {

        private var accessToken: String? = null
        private var username: String? = null
        private var groups: Collection<Group>? = null
        private var tasks: Collection<Task>? = null
        private var labels: Collection<Label>? = null

        fun accessToken(accessToken: String) = apply { this.accessToken = accessToken }

        fun username(username: String) = apply { this.username = username }

        fun groups(groups: Collection<Group>) = apply { this.groups = groups }

        fun tasks(tasks: Collection<Task>) = apply { this.tasks = tasks }

        fun labels(labels: Collection<Label>) = apply { this.labels = labels }

        fun build() = AdminModel(
            accessToken ?: throw TrikException(String.format(PARAMETER_ERROR, AdminModel::accessToken.name)),
            username ?: throw TrikException(String.format(PARAMETER_ERROR, AdminModel::username.name)),
            groups ?: throw TrikException(String.format(PARAMETER_ERROR, AdminModel::groups.name)),
            tasks ?: throw TrikException(String.format(PARAMETER_ERROR, AdminModel::tasks.name)),
            labels ?: throw TrikException(String.format(PARAMETER_ERROR, AdminModel::labels.name))
        )
    }

    override fun asMap(): Map<String, Any?> {
        val argsMap = mutableMapOf<String, Any?>()

        argsMap[this::accessToken.name] = accessToken
        argsMap[this::username.name] = username
        argsMap[this::groups.name] = groups.sortedBy { it.id }
        argsMap[this::tasks.name] = tasks.sortedBy { it.id }
        argsMap[this::labels.name] = labels.sortedBy { it.id }

        return argsMap
    }

    companion object {
        private const val PARAMETER_ERROR = "Parameter '%s' must be specified."
    }
}