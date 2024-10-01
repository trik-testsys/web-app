package trik.testsys.webclient.view

import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.impl.Admin

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
data class GroupCreationView(
    val name: String,
    val additionalInfo: String
) {

    fun toEntity(regToken: String, admin: Admin) = Group(
        name, regToken
    ).also {
        it.admin = admin
        it.additionalInfo = additionalInfo
    }

    companion object {

        fun empty() = GroupCreationView("", "")
    }
}