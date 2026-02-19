package trik.testsys.webapp.backoffice.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Converter
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.MappedSuperclass
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.utils.enums.PersistableEnum
import trik.testsys.webapp.core.utils.enums.converter.AbstractPersistableEnumConverter

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@MappedSuperclass
abstract class Token(
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    val type: Type
) : AbstractEntity() {

    @Column(name = "value", nullable = false)
    var value: String? = null
        set(value) {
            val prefix = type.dbKey.lowercase()
            field = "$prefix-$value"
        }

    @Suppress("unused")
    enum class Type(override val dbKey: String) : PersistableEnum {

        REGISTRATION("REG"),
        ACCESS("ACS"),
        STUDENT_GROUP("STG");

        companion object {

            @Converter(autoApply = true)
            class JpaConverter : AbstractPersistableEnumConverter<Type>()
        }
    }
}