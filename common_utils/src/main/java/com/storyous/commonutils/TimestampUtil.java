package com.storyous.commonutils;

import android.content.Context;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

public enum TimestampUtil {
   INSTANCE;

   private static boolean useKronos = false;

   public static void setUseKronos(boolean value) {
      useKronos = value;
      Timber.tag("Kronos").i("Set" + (value ? "" : " not") + " to use Kronos");
   }

   public synchronized void init(Context context) {
      TimestampUtilKronos.INSTANCE.init(context);
      TimestampUtilTruetime.INSTANCE.init(context);
   }

   public Date getNow() {
      return useKronos ? TimestampUtilKronos.INSTANCE.getNow() : TimestampUtilTruetime.INSTANCE.getNow();
   }

   public static synchronized long getTime() {
      return useKronos ? TimestampUtilKronos.getTime() : TimestampUtilTruetime.getTime();
   }

   public static synchronized Date getDate() {
      return useKronos ? TimestampUtilKronos.getDate() : TimestampUtilTruetime.getDate();
   }

   public static synchronized Calendar getCalendar() {
      return useKronos ? TimestampUtilKronos.getCalendar() : TimestampUtilTruetime.getCalendar();
   }

   public static synchronized long getTimeAgo(long millisAgo) {
      return useKronos ? TimestampUtilKronos.getTimeAgo(millisAgo) : TimestampUtilTruetime.getTimeAgo(millisAgo);
   }

   public static Date parseDate(String date) throws ParseException {
      return useKronos ? TimestampUtilKronos.parseDate(date) : TimestampUtilTruetime.parseDate(date);
   }

   public synchronized void start(Context context) {
      TimestampUtilKronos.INSTANCE.start(context);
      TimestampUtilTruetime.INSTANCE.start(context);
   }

   public synchronized void stop(Context context) {
      TimestampUtilKronos.INSTANCE.stop(context);
      TimestampUtilTruetime.INSTANCE.stop(context);
   }
}
