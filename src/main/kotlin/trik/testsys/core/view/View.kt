package trik.testsys.core.view

import trik.testsys.core.entity.Entity
import java.util.TimeZone

/**
 * @author Roman Shishkin
 * @since 2.0.0
**/
interface View<T : Entity> {

    val id: Long?

    fun toEntity(timeZone: TimeZone): T
}