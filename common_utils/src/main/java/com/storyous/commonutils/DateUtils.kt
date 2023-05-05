package com.storyous.commonutils

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    val YMDHMS get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val YMDHMS_DOTS get() = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US)
    val YMD get() = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val ISO8601 get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
    val ISO8601_NOZONE get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    val ISO8601_FRACT_NOZONE get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    val ISO8601_FRACT_NOZONE_UTC
        get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .also { it.timeZone = TimeZone.getTimeZone("UTC") }
    val ISO8601_FRACT get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ", Locale.US)
    val DMY_SPACED get() = SimpleDateFormat("dd. MM. yyyy", Locale.US)
    val DMYHM get() = SimpleDateFormat("d.M.yyyy HH:mm", Locale.US)
    val DMYHM_PL get() = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
    val HM get() = SimpleDateFormat("HH:mm", Locale.US)
    val HMS get() = SimpleDateFormat("HH:mm:ss", Locale.US)
    val DMY get() = SimpleDateFormat("d.M.yyyy", Locale.US)
    val DMYHMS get() = SimpleDateFormat("d.M.yyyy H:mm:ss", Locale.US)
    val simpleDate get() = SimpleDateFormat.getDateInstance(DateFormat.DEFAULT)
}

@Throws(ParseException::class)
fun String?.parseYMDHMSDate(): Date? {
    return this?.let { DateUtils.YMDHMS.parse(it) }
}

@Throws(ParseException::class)
fun String?.parseISO8601FractNozoneUTCDate(): Date? {
    return this?.let { DateUtils.ISO8601_FRACT_NOZONE_UTC.parse(it) }
}
