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
fun LocalDateTime.atTimeZone(timeZoneId: String?): LocalDateTime =
    atZone(AbstractEntity.DEFAULT_ZONE_ID).withZoneSameInstant(TimeZone.getTimeZone(timeZoneId ?: "UTC").toZoneId())
        .toLocalDateTime()

/**
 * Converts [this] from [timeZone] zone to [AbstractEntity.DEFAULT_ZONE_ID].
 *
 * @author Roman Shishkin
 * @since 2.0.0
 **/
fun LocalDateTime.fromTimeZone(timeZoneId: String?): LocalDateTime =
    atZone(TimeZone.getTimeZone(timeZoneId ?: "UTC").toZoneId()).withZoneSameInstant(AbstractEntity.DEFAULT_ZONE_ID)
        .toLocalDateTime()