package com.storyous.commonutils

import org.junit.Assert
import org.junit.Test
import java.text.ParseException
import java.util.Calendar

class DateUtilsKtTest {

    @Suppress("Detekt.MagicNumber")
    @Test
    fun parseYMDHMSDate() {
        val date = Calendar.getInstance()
        date.time = "2019-10-10 10:10:10".parseYMDHMSDate()
        Assert.assertEquals(2019, date.get(Calendar.YEAR))
        Assert.assertEquals(10, date.get(Calendar.MONTH) + 1)
        Assert.assertEquals(10, date.get(Calendar.DAY_OF_MONTH))
        Assert.assertEquals(10, date.get(Calendar.HOUR))
        Assert.assertEquals(10, date.get(Calendar.MINUTE))
        Assert.assertEquals(10, date.get(Calendar.SECOND))
    }

    @Test(expected = ParseException::class)
    fun parseYMDHMSDateParseException() {
        "sdfbsdf".parseYMDHMSDate()
    }

    @Test
    fun parseYMDHMSDateNull() {
        Assert.assertTrue(null.parseYMDHMSDate() == null)
    }
}
