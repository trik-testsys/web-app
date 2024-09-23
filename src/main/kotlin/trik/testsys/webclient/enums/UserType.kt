package trik.testsys.webclient.enums

import trik.testsys.core.utils.enums.converter.AbstractEnumConverter
import trik.testsys.core.utils.enums.Enum
import javax.persistence.Converter

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