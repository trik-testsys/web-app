package trik.testsys.webclient.util

import trik.testsys.core.entity.AbstractEntity
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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

fun LocalDateTime.format(): String = format(DEFAULT_FORMATTER)

fun LocalDateTime.formatDate(): String = format(DEFAULT_DATE_FORMATTER)

fun LocalTime.format(): String = format(DEFAULT_TIME_FORMATTER)

val LocalDateTime.DEFAULT_FORMATTER: DateTimeFormatter
    get() = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

val LocalDateTime.DEFAULT_DATE_FORMATTER: DateTimeFormatter
    get() = DateTimeFormatter.ofPattern("dd.MM.yyyy")

val LocalTime.DEFAULT_TIME_FORMATTER: DateTimeFormatter
    get() = DateTimeFormatter.ofPattern("HH:mm:ss")

fun LocalDateTime.isBeforeOrEqual(other: LocalDateTime): Boolean = this.isBefore(other) || this == other

fun LocalDateTime.isAfterOrEqual(other: LocalDateTime): Boolean = this.isAfter(other) || this == other

fun LocalDateTime.isBeforeOrEqualNow(): Boolean = isBeforeOrEqual(LocalDateTime.now(AbstractEntity.DEFAULT_ZONE_ID))

fun LocalDateTime.isAfterOrEqualNow(): Boolean = isAfterOrEqual(LocalDateTime.now(AbstractEntity.DEFAULT_ZONE_ID))

fun LocalDateTime.isBeforeNow(): Boolean = isBefore(LocalDateTime.now(AbstractEntity.DEFAULT_ZONE_ID))

fun LocalDateTime.isAfterNow(): Boolean = isAfter(LocalDateTime.now(AbstractEntity.DEFAULT_ZONE_ID))

fun LocalDateTime.toEpochSecond(): Long = toEpochSecond(ZoneOffset.UTC)