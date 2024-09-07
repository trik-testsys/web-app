package trik.testsys.webclient.model.impl

import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.impl.Label
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.model.TrikModel
import trik.testsys.webclient.util.exception.impl.TrikIllegalStateException
import java.time.LocalDateTime

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
class AdminModel private constructor(
    val accessToken: String,
    val username: String,
    val groups: Collection<Group>,
    val tasks: Collection<Task>,
    val labels: Collection<Label>,
    val webUserId: Long?,
    val additionalInfo: String,
    val registrationDate: LocalDateTime,
    val lastLoginDate: LocalDateTime?
) : TrikModel {

    class Builder internal constructor() {

        private var accessToken: String? = null
        private var username: String? = null
        private var groups: Collection<Group>? = null
        private var tasks: Collection<Task>? = null
        private var labels: Collection<Label>? = null
        private var webUserId: Long? = null
        private var additionalInfo: String? = null
        private var registrationDate: LocalDateTime? = null
        private var lastLoginDate: LocalDateTime? = null

        fun accessToken(accessToken: String) = apply { this.accessToken = accessToken }

        fun username(username: String) = apply { this.username = username }

        fun groups(groups: Collection<Group>) = apply { this.groups = groups }

        fun tasks(tasks: Collection<Task>) = apply { this.tasks = tasks }

        fun labels(labels: Collection<Label>) = apply { this.labels = labels }

        fun webUserId(webUserId: Long?) = apply { this.webUserId = webUserId }

        fun additionalInfo(additionalInfo: String?) = apply { this.additionalInfo = additionalInfo }

        fun registrationDate(registrationDate: LocalDateTime?) = apply { this.registrationDate = registrationDate }

        fun lastLoginDate(lastLoginDate: LocalDateTime?) = apply { this.lastLoginDate = lastLoginDate }

        fun build() = AdminModel(
            accessToken ?: throw TrikIllegalStateException(String.format(PARAMETER_ERROR, AdminModel::accessToken.name)),
            username ?: throw TrikIllegalStateException(String.format(PARAMETER_ERROR, AdminModel::username.name)),
            groups ?: throw TrikIllegalStateException(String.format(PARAMETER_ERROR, AdminModel::groups.name)),
            tasks ?: throw TrikIllegalStateException(String.format(PARAMETER_ERROR, AdminModel::tasks.name)),
            labels ?: throw TrikIllegalStateException(String.format(PARAMETER_ERROR, AdminModel::labels.name)),
            webUserId,
            additionalInfo ?: "",
            registrationDate ?: throw TrikIllegalStateException(String.format(PARAMETER_ERROR, AdminModel::registrationDate.name)),
            lastLoginDate
        )
    }

    override fun asMap(): Map<String, Any?> {
        val argsMap = mutableMapOf<String, Any?>()

        argsMap[this::accessToken.name] = accessToken
        argsMap[this::username.name] = username
        argsMap[this::groups.name] = groups.sortedBy { it.id }
        argsMap[this::tasks.name] = tasks.sortedBy { it.id }
        argsMap[this::labels.name] = labels.sortedBy { it.id }
        argsMap[this::webUserId.name] = webUserId
        argsMap[this::additionalInfo.name] = additionalInfo
        argsMap[this::registrationDate.name] = registrationDate
        argsMap[this::lastLoginDate.name] = lastLoginDate

        return argsMap
    }

    companion object {
        private const val PARAMETER_ERROR = "Parameter '%s' must be specified."
    }
}