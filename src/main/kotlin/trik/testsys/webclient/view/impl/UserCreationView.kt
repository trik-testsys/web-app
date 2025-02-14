package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.user.WebUser

/**
 * @author Roman Shishkin
 * @since 2.2.0
 */
data class UserCreationView(
    val name: String = "",
    val additionalInfo: String = "",
    val type: WebUser.UserType,
    val viewerId: Long? = null
) {

    companion object {

        fun emptyViewer() = UserCreationView(type = WebUser.UserType.VIEWER)

        fun emptyAdmin() = UserCreationView(type = WebUser.UserType.ADMIN)

        fun emptyDeveloper() = UserCreationView(type = WebUser.UserType.DEVELOPER)

        fun emptyJudge() = UserCreationView(type = WebUser.UserType.JUDGE)
    }
}
