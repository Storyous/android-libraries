package com.storyous.commonutils

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Honza Bartovský on 15.03.2019.
 */
object DateUtils {
    val YMDHMS by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }
    val YMDHMS_DOTS by lazy { SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US) }
    val YMD by lazy { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val ISO8601 by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US) }
    val ISO8601_NOZONE by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US) }
    val ISO8601_FRACT_NOZONE by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US) }
    val ISO8601_FRACT_NOZONE_UTC by lazy {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .also { it.timeZone = TimeZone.getTimeZone("UTC") }
    }
    val ISO8601_FRACT by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ", Locale.US) }
    val DMY_SPACED by lazy { SimpleDateFormat("dd. MM. yyyy", Locale.US) }
    val DMYHM by lazy { SimpleDateFormat("d.M.yyyy HH:mm", Locale.US) }
    val DMYHM_PL by lazy { SimpleDateFormat("dd.MM.yyyy HH:mm") }
    val HM by lazy { SimpleDateFormat("HH:mm", Locale.US) }
    val HMS by lazy { SimpleDateFormat("HH:mm:ss", Locale.US) }
    val DMY by lazy { SimpleDateFormat("d.M.yyyy", Locale.US) }
    val DMYHMS by lazy { SimpleDateFormat("d.M.yyyy H:mm:ss", Locale.US) }
    val simpleDate by lazy { SimpleDateFormat.getDateInstance(DateFormat.DEFAULT) }
}

@Throws(ParseException::class)
fun String?.parseYMDHMSDate(): Date? {
    return this?.let { DateUtils.YMDHMS.parse(it) }
}

@Throws(ParseException::class)
fun String?.parseISO8601FractNozoneUTCDate(): Date? {
    return this?.let { DateUtils.ISO8601_FRACT_NOZONE_UTC.parse(it) }
}
