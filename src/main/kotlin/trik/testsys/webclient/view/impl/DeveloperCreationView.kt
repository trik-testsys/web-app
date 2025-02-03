package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.user.impl.Developer

/**
 * @author Roman Shishkin
 * @since 2.1.0
 */
data class DeveloperCreationView(
    val name: String,
    val additionalInfo: String
) {

    fun toEntity(accessToken: String) = Developer(
        name, accessToken
    ).also {
        it.additionalInfo = additionalInfo
    }

    companion object {

        fun empty() = DeveloperCreationView("", "")
    }
}