package trik.testsys.core.view.user

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.entity.user.UserEntity
import trik.testsys.core.view.View
import java.time.LocalDateTime

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
interface UserView<E : UserEntity> : View<E> {

    val name: String

    val accessToken: AccessToken

    val lastLoginDate: LocalDateTime
}