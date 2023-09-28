package trik.testsys.webclient.model.impl

import trik.testsys.webclient.controller.impl.ViewerController
import trik.testsys.webclient.entity.impl.Admin
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.model.TrikModel
import trik.testsys.webclient.util.exception.TrikException

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
class ViewerModel private constructor(
    val accessToken: String,
    val adminRegToken: String?,
    val username: String?,
    val admins: Collection<Admin>,
    val groups: Collection<Group>,
    val groupsResult: Map<Long, ViewerController.Table>,
    val adminsResult: Map<Long, ViewerController.Table>
) : TrikModel {

    class Builder internal constructor() {

        private var accessToken: String? = null
        private var adminRegToken: String? = null
        private var username: String? = null
        private var admins: Collection<Admin>? = null
        private var groups: Collection<Group>? = null
        private var groupsResult: Map<Long, ViewerController.Table>? = null
        private var adminsResult: Map<Long, ViewerController.Table>? = null

        fun accessToken(accessToken: String) = apply { this.accessToken = accessToken }

        fun adminRegToken(adminRegToken: String) = apply { this.adminRegToken = adminRegToken }

        fun username(username: String) = apply { this.username = username }

        fun admins(admins: Collection<Admin>) = apply { this.admins = admins }

        fun groups(groups: Collection<Group>) = apply { this.groups = groups }

        fun groupsResult(groupsResult: Map<Long, ViewerController.Table>) = apply { this.groupsResult = groupsResult }

        fun adminsResult(adminsResult: Map<Long, ViewerController.Table>) = apply { this.adminsResult = adminsResult }

        fun build() = ViewerModel(
            accessToken ?: throw TrikException(String.format(PARAMETER_ERROR, ViewerModel::accessToken.name)),
            adminRegToken,
            username,
            admins ?: throw TrikException(String.format(PARAMETER_ERROR, ViewerModel::admins.name)),
            groups ?: throw TrikException(String.format(PARAMETER_ERROR, ViewerModel::groups.name)),
            groupsResult ?: throw TrikException(String.format(PARAMETER_ERROR, ViewerModel::groupsResult.name)),
            adminsResult ?: throw TrikException(String.format(PARAMETER_ERROR, ViewerModel::adminsResult.name))
        )
    }

    override fun asMap(): Map<String, Any?> {
        val argsMap = mutableMapOf<String, Any?>()

        argsMap[this::accessToken.name] = accessToken
        argsMap[this::adminRegToken.name] = adminRegToken
        argsMap[this::username.name] = username
        argsMap[this::admins.name] = admins.sortedBy { it.id }
        argsMap[this::groups.name] = groups.sortedBy { it.admin.id }
        argsMap[this::groupsResult.name] = groupsResult
        argsMap[this::adminsResult.name] = adminsResult

        return argsMap
    }

    companion object {
        private const val PARAMETER_ERROR = "Parameter '%s' must be specified."
    }
}