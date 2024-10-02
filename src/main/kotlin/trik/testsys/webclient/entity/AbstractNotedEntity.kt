package trik.testsys.webclient.entity

import trik.testsys.core.entity.named.AbstractNamedEntity
import javax.persistence.Column
import javax.persistence.MappedSuperclass

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
@MappedSuperclass
abstract class AbstractNotedEntity(
    name: String
) : NotedEntity, AbstractNamedEntity(name) {

    @Column(
        nullable = false, unique = false, updatable = true,
        length = NOTE_MAX_LEN
    ) override var note: String = NOTE_DEFAULT

    companion object {

        /**
         * Maximum length of the [note] property.
         *
         * @author Roman Shishkin
         * @since 2.0.0
         */
        private const val NOTE_MAX_LEN = 255

        private const val NOTE_DEFAULT = ""
    }
}