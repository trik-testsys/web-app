package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.impl.EmergencyMessage
import trik.testsys.webclient.entity.user.WebUser

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
data class EmergencyMessageCreationView(
    val userType: WebUser.UserType,
    val additionalInfo: String
) {

    fun toEntity() = EmergencyMessage(
        userType
    ).also {
        it.additionalInfo = additionalInfo
    }

    companion object {

        fun empty() = EmergencyMessageCreationView(WebUser.UserType.SUPER_USER, "")
    }
}