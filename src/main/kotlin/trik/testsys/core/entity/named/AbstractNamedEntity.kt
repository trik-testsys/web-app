package trik.testsys.core.entity.named

import trik.testsys.core.entity.AbstractEntity
import javax.persistence.Column
import javax.persistence.MappedSuperclass

/**
 * Simple abstract named entity class which extends [AbstractEntity] and implements [NamedEntity].
 *
 * @see NamedEntity
 * @see AbstractEntity
 *
 * @author Roman Shishkin
 * @since 2.0.0
 **/
@MappedSuperclass
abstract class AbstractNamedEntity(
    @Column(
        nullable = false, unique = false, updatable = true,
        length = NAME_MAX_LEN
    ) override var name: String
) : NamedEntity, AbstractEntity() {

    companion object {

        /**
         * Maximum length of the [name] property.
         *
         * @author Roman Shishkin
         * @since 2.0.0
         */
        private const val NAME_MAX_LEN = 127
    }
}