package trik.testsys.webclient.model

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
class DeveloperModel private constructor(
    val accessToken: String,
    val username: String,
) {

    class Builder internal constructor() {

        private var accessToken: String? = null
        private var username: String? = null

        fun accessToken(accessToken: String) = apply { this.accessToken = accessToken }

        fun username(username: String) = apply { this.username = username }


        fun build(): DeveloperModel {
            requireNotNull(accessToken) { "Access token must be specified." }
            requireNotNull(username) { "Username must be specified." }

            return DeveloperModel(
                accessToken = accessToken!!,
                username = username!!,
            )
        }
    }

    fun asMap(): Map<String, Any?> {
        val argsMap = mutableMapOf<String, Any?>()

        argsMap[this::accessToken.name] = accessToken
        argsMap[this::username.name] = username

        return argsMap
    }
}
