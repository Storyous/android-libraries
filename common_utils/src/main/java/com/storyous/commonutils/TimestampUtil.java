package com.storyous.commonutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import com.lyft.kronos.AndroidClockFactory;
import com.lyft.kronos.KronosClock;
import com.lyft.kronos.SyncListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

public enum TimestampUtil {
    INSTANCE;

    private BroadcastReceiver mConnectionReinitReceiver;
    private KronosClock mKronosClock;
    private ArrayList<String> mNtpHosts;

    public void receiveBroadcast(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected() && activeNetwork.isAvailable()) {
            init(context.getApplicationContext());
        }
    }

    public synchronized void init(Context context) {
        Timber.tag("Kronos").i("Start Kronos init.");
        if (mNtpHosts == null) {
            mNtpHosts = new ArrayList<String>();
            mNtpHosts.add("cz.pool.ntp.org");
            mNtpHosts.add("0.pool.ntp.org");
            mNtpHosts.add("1.pool.ntp.org");
            mNtpHosts.add("2.pool.ntp.org");
            mNtpHosts.add("3.pool.ntp.org");
        }
        if (mKronosClock == null) {
            mKronosClock = AndroidClockFactory.createKronosClock(
                    context,
                    new SyncListener() {
                        @Override
                        public void onSuccess(long ticksDelta, long responseTimeMs) {
                            Timber.tag("Kronos").i(
                                    "NTP time was initialized %s vs system time %s, delta: %d, response: %d ms",
                                    format(new Date(mKronosClock.getCurrentTimeMs())), format(new Date()), ticksDelta, responseTimeMs
                            );
                        }

                        @Override
                        public void onStartSync(@NonNull String host) {
                            Timber.tag("Kronos").i("Syncing NTP with: %s", host);
                        }

                        @Override
                        public void onError(@NonNull String host, @NonNull Throwable throwable) {
                            Timber.tag("Kronos").e(throwable, "Failed to sync NTP with the host: %s", host);
                        }

                        private String format(Date date) {
                            return DateUtils.INSTANCE.getISO8601_FRACT().format(date);
                        }
                    }
            );
        }
        mKronosClock.syncInBackground();
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    // third party library can throw any exception possible
    public Date getNow() {
        return mKronosClock == null ? new Date() : new Date(mKronosClock.getCurrentTimeMs());
    }

    public static synchronized long getTime() {
        return INSTANCE.getNow().getTime();
    }

    public static synchronized Date getDate() {
        return INSTANCE.getNow();
    }

    public static synchronized Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(INSTANCE.getNow());
        return calendar;
    }

    public static synchronized long getTimeAgo(long millisAgo) {
        return INSTANCE.getNow().getTime() - millisAgo;
    }

    public static Date parseDate(String date) throws ParseException {
        return DateUtils.INSTANCE.getISO8601_FRACT().parse(date);
    }

    public synchronized void start(Context context) {
        if (mConnectionReinitReceiver == null) {
            mConnectionReinitReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    receiveBroadcast(context);
                }
            };

            context.getApplicationContext()
                    .registerReceiver(mConnectionReinitReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }

    public synchronized void stop(Context context) {
        if (mConnectionReinitReceiver != null) {
            try {
                context.getApplicationContext()
                        .unregisterReceiver(mConnectionReinitReceiver);
            } catch (IllegalArgumentException ex) {
                Timber.e(ex, "TimestampUtil connectivity receiver not registered");
            }
            mConnectionReinitReceiver = null;
        }
    }
}
