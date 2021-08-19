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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public enum TimestampUtilKronos {
    INSTANCE;

    private static final String TIMBER_TAG = "Kronos";
    private static final List<String> NTP_HOSTS = Arrays.asList(
            "cz.pool.ntp.org", "0.pool.ntp.org", "1.pool.ntp.org", "2.pool.ntp.org", "3.pool.ntp.org"
    );

    private BroadcastReceiver mConnectionReinitReceiver;
    private KronosClock mKronosClock;

    public void receiveBroadcast(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected() && activeNetwork.isAvailable()) {
            init(context.getApplicationContext());
        }
    }

    public synchronized void init(Context context) {
        if (mKronosClock == null) {
            mKronosClock = AndroidClockFactory.createKronosClock(
                    context,
                    new SyncListener() {
                        @Override
                        public void onSuccess(long ticksDelta, long responseTimeMs) {
                            Timber.tag(TIMBER_TAG).i(
                                    "NTP time was initialized %s vs system time %s, delta: %d, response: %d ms",
                                    format(new Date(mKronosClock.getCurrentTimeMs())), format(new Date()), ticksDelta, responseTimeMs
                            );
                        }

                        @Override
                        public void onStartSync(@NonNull String host) {
                            Timber.tag(TIMBER_TAG).i("Syncing NTP with: %s", host);
                        }

                        @Override
                        public void onError(@NonNull String host, @NonNull Throwable throwable) {
                            Timber.tag(TIMBER_TAG).e("Failed to sync NTP with the host: %s, message: %s", host, throwable.getMessage());
                        }

                        private String format(Date date) {
                            return DateUtils.INSTANCE.getISO8601_FRACT().format(date);
                        }
                    },
                    NTP_HOSTS
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
