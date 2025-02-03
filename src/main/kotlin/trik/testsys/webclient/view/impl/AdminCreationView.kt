package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.service.entity.user.impl.ViewerService

/**
 * @author Roman Shishkin
 * @since 2.1.0
 **/
data class AdminCreationView(
    val name: String,
    val additionalInfo: String,
    val viewerId: Long?
) {

    fun toEntity(accessToken: String, viewerService: ViewerService) = Admin(
        name, accessToken
    ).also {
        it.additionalInfo = additionalInfo
        it.viewer = viewerService.find(viewerId)!!
    }

    companion object {

        fun empty() = AdminCreationView("", "", null)
    }
}