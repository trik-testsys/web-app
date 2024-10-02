package trik.testsys.webclient.entity.user

import trik.testsys.core.entity.user.AbstractUser
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.utils.enums.Enum
import trik.testsys.core.utils.enums.converter.AbstractEnumConverter
import javax.persistence.Column
import javax.persistence.Converter
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

    /**
     * User type enum class.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    enum class UserType(override val dbkey: String) : Enum {

        ADMIN("ADM"),
        DEVELOPER("DEV"),
        JUDGE("JDG"),
        STUDENT("STT"),
        SUPER_USER("SUR"),
        VIEWER("VWR");

        companion object {

            @Converter(autoApply = true)
            class UserTypeConverter : AbstractEnumConverter<UserType>()
        }
    }
}
