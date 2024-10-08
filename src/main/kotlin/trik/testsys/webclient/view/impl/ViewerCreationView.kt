package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.user.impl.Viewer

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
data class ViewerCreationView(
    val name: String,
    val additionalInfo: String
) {

    fun toEntity(accessToken: String, regToken: String) = Viewer(
        name, accessToken, regToken
    ).also {
        it.additionalInfo = additionalInfo
    }

    companion object {

        fun empty() = ViewerCreationView("", "")
    }
}