package trik.testsys.webclient.entity.user

import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.enums.UserType
import javax.persistence.Column
import javax.persistence.MappedSuperclass

/**
 * Web user abstract class which extends [AbstractUser] with additional [type] field.
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
@MappedSuperclass
abstract class WebUser(
    name: String,
    accessToken: AccessToken,

    @Column(nullable = false, unique = false, updatable = false)
    val type: UserType
) : AbstractUser(name, accessToken) {
}