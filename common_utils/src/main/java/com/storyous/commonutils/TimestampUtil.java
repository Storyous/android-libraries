package com.storyous.commonutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.instacart.library.truetime.TrueTime;
import com.instacart.library.truetime.TrueTimeRx;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public enum TimestampUtil {
    INSTANCE;

    private BroadcastReceiver mConnectionReinitReceiver;
    private TimeObserver mObserver;

    public void receiveBroadcast(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected() && activeNetwork.isAvailable()) {
            init(context.getApplicationContext());
        }
    }

    public synchronized void init(Context context) {
        if (mObserver != null) {
            mObserver.dispose();
        }

        Timber.i("Start TrueTime init.");
        mObserver = TrueTimeRx.build()
                .withSharedPreferencesCache(context)
                .initializeNtp("cz.pool.ntp.org")
                .map(longs -> TrueTimeRx.now())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new TimeObserver());
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    // third party library can throw any exception possible
    public Date getNow() {
        if (TrueTime.isInitialized()) {
            try {
                return TrueTimeRx.now();
            } catch (RuntimeException ex) {
                Timber.e(ex, "Could not initialize TrueTime");
            }
        }
        return new Date();
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
        if (mObserver != null) {
            mObserver.dispose();
        }

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

    private static class TimeObserver extends DisposableSingleObserver<Date> {
        @Override
        public void onSuccess(Date date) {
            Timber.i(
                    "NTP time was initialized %s vs system time %s",
                    format(date),
                    format(new Date())
            );
        }

        @Override
        public void onError(Throwable e) {
            Timber.i(
                    "NTP time error occured: %s, current %s, system time %s",
                    e.getMessage(),
                    TrueTime.isInitialized() ? format(TrueTimeRx.now()) : "not initialized",
                    format(new Date())
            );
        }

        private String format(Date date) {
            return DateUtils.INSTANCE.getISO8601_FRACT().format(date);
        }
    }
}
