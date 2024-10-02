package trik.testsys.core.utils.enums.converter

import trik.testsys.core.utils.enums.Enum
import java.lang.reflect.ParameterizedType
import javax.persistence.AttributeConverter

/**
 * Simple enum jpa converter.
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
abstract class AbstractEnumConverter<T : Enum> : AttributeConverter<T, String> {

    private val enumClass = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>

    override fun convertToDatabaseColumn(attribute: T?) = attribute?.dbkey

    override fun convertToEntityAttribute(dbData: String?): T? {
        dbData ?: return null
        enumClass.enumConstants.forEach { value ->
            if (value.dbkey == dbData) return value
        }

        throw IllegalArgumentException("Invalid value $dbData for enum class $${enumClass.simpleName}.")
    }
}