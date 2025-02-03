package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.user.impl.Judge

/**
 * @author Roman Shishkin
 * @since 2.1.0
 */
data class JudgeCreationView(
    val name: String,
    val additionalInfo: String
) {

    fun toEntity(accessToken: String) = Judge(
        name, accessToken
    ).also {
        it.additionalInfo = additionalInfo
    }

    companion object {

        fun empty() = JudgeCreationView("", "")
    }
}