package trik.testsys.webclient.util

import trik.testsys.core.entity.AbstractEntity
import java.time.LocalDateTime
import java.util.TimeZone

/**
 * Converts [this] from [AbstractEntity.DEFAULT_ZONE_ID] to [timeZone] zone.
 *
 * @author Roman Shishkin
 * @since 2.0.0
**/
fun LocalDateTime.atTimeZone(timeZone: TimeZone): LocalDateTime =
    atZone(AbstractEntity.DEFAULT_ZONE_ID).withZoneSameInstant(timeZone.toZoneId()).toLocalDateTime()

/**
 * Converts [this] from [timeZone] zone to [AbstractEntity.DEFAULT_ZONE_ID].
 *
 * @author Roman Shishkin
 * @since 2.0.0
**/
fun LocalDateTime.fromTimeZone(timeZone: TimeZone): LocalDateTime =
    atZone(timeZone.toZoneId()).withZoneSameInstant(AbstractEntity.DEFAULT_ZONE_ID).toLocalDateTime()