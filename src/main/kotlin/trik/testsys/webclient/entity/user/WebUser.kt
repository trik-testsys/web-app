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
    override var name: String,
    override var accessToken: AccessToken
) : AbstractUser(name, accessToken) {

    @get:Column(nullable = false, unique = false)
    abstract val type: UserType
}