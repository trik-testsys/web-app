package trik.testsys.core.view.user

import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.entity.user.UserEntity
import trik.testsys.core.view.named.NamedEntityView
import java.time.LocalDateTime

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
interface UserView<E : UserEntity> : NamedEntityView<E> {

    val accessToken: AccessToken

    val lastLoginDate: LocalDateTime?
}