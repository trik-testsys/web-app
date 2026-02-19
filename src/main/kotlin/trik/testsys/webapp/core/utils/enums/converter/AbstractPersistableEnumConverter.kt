package trik.testsys.webapp.core.utils.enums.converter

import jakarta.persistence.AttributeConverter
import trik.testsys.webapp.core.utils.enums.PersistableEnum
import java.lang.IllegalStateException
import java.lang.reflect.ParameterizedType

/**
 * Generic JPA AttributeConverter that maps enums implementing [PersistableEnum]
 * to their dbkey representation and back.
 *
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Suppress("unused")
abstract class AbstractPersistableEnumConverter<E> : AttributeConverter<E, String>
        where E : Enum<E>,
              E : PersistableEnum {

    @Suppress("UNCHECKED_CAST")
    private val enumClass: Class<E> by lazy {
        val superType = javaClass.genericSuperclass
        val type = (superType as ParameterizedType).actualTypeArguments[0]
        when (type) {
            is Class<*> -> type as Class<E>
            is ParameterizedType -> type.rawType as Class<E>
            else -> throw IllegalStateException("Cannot determine enum type for converter ${javaClass.name}")
        }
    }

    override fun convertToDatabaseColumn(attribute: E?): String? = attribute?.dbKey

    override fun convertToEntityAttribute(dbData: String?): E? {
        if (dbData == null) return null
        val all = enumClass.enumConstants ?: emptyArray()
        return all.firstOrNull { it.dbKey == dbData }
            ?: throw IllegalArgumentException("Unknown dbkey '$dbData' for enum ${enumClass.name}")
    }
}


