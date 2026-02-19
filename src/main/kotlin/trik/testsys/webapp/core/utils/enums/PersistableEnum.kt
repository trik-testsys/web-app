package trik.testsys.webapp.core.utils.enums

/**
 * Marker for enums that are stored in DB via a short stable key.
 *
 * @author Roman Shishkin
 * @since 3.12.0
 */
interface PersistableEnum {

    /**
     * Short, stable key stored in DB instead of the enum name.
     *
     * @author Roman Shishkin
     * @since 3.12.0
     */
    val dbKey: String
}


