package trik.testsys.webclient.model

import trik.testsys.webclient.util.exception.TrikException

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
class DeveloperModel private constructor(
    val accessToken: String,
    val username: String,
    val postTaskMessage: String?,
) {

    class Builder internal constructor() {

        private var accessToken: String? = null
        private var username: String? = null
        private var postTaskMessage: String? = null

        fun accessToken(accessToken: String) = apply { this.accessToken = accessToken }

        fun username(username: String) = apply { this.username = username }

        fun postTaskMessage(postTaskMessage: String) = apply { this.postTaskMessage = postTaskMessage }

        fun build() = DeveloperModel(
            accessToken ?: throw TrikException(String.format(PARAMETER_ERROR, DeveloperModel::accessToken.name)),
            username ?: throw TrikException(String.format(PARAMETER_ERROR, DeveloperModel::username.name)),
            postTaskMessage
        )

    }

    fun asMap(): Map<String, Any?> {
        val argsMap = mutableMapOf<String, Any?>()

        argsMap[this::accessToken.name] = accessToken
        argsMap[this::username.name] = username

        return argsMap
    }

    companion object {
        private const val PARAMETER_ERROR = "Parameter '%s' must be specified."
    }
}
