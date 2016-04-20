package com.zahiar.countdowntimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

/**
 * This service will be used to keep track of the timer set by the user, and
 * once it is up, a dialog will be shown to the user.
 *
 * @author Zahiar Ahmed
 */
public class TimerService extends Service {
    public static final String BROADCAST_ACTION_TIMER_RUNNING =
        "com.zahiar.countdowntimer.TIMER_RUNNING";
    public static final String BROADCAST_ACTION_TIMER_FINISHED =
        "com.zahiar.countdowntimer.TIMER_FINISHED";
    public static final String INTENT_COUNTDOWN_VALUE = "countdown_value";

    private static final int MILLISECS_PER_SEC = 1000;
    private static final int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;
    private CountDownTimer mCountDownTimer;
    private LocalBroadcastManager mLocalBroadcastManager;
    private long duration;

    /**
     * Initialises the service by initialising both the LocalBroadCast and
     * Notification managers.
     */
    @Override
    public void onCreate() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        // Gets a handle to the system mNotification service.
        mNotificationManager = (NotificationManager)
            getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * This method handles the starting of the service.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);

        return START_STICKY;
    }

    /**
     * This method handles the shutting down of the service by terminating the
     * CountdownTimer and removing any existing notifications.
     */
    @Override
    public void onDestroy() {
        // Cancel the countdown timer
        mCountDownTimer.cancel();

        // Cancels the status bar, mNotification
        mNotificationManager.cancel(NOTIFICATION_ID);

        super.onDestroy();
    }

    /**
     * This method allows the service to be binded to but as we don't
     * need it therefore it will not be used.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This method is responsible for launching the CountDownTimer and for
     * displaying notifications to the user.
     *
     * This method will also validate any extra data supplied with the Intent,
     * if the data is not valid, the service will immediately abort.
     *
     * @param intent The Intent supplied to the startService(Intent), when the
     *               service was first started.
     */
    private void handleCommand(Intent intent) {
        duration = intent.getLongExtra(MainActivity.DURATION_VALUE, -1)
            * MILLISECS_PER_SEC;

        // If the duration is invalid, immediately end the service
        if (duration < 1) {
            stopSelf();
        }

        final Context c = this;

        // Creates a new countdown timer
        mCountDownTimer = new CountDownTimer(duration, 500) {
            Intent mUpdateUIIntent = new Intent(BROADCAST_ACTION_TIMER_RUNNING);
            Intent mTimerDialogIntent = new Intent(c, TimerDialogActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            @Override
            public void onTick(long millisUntilFinished) {
                mUpdateUIIntent.putExtra(INTENT_COUNTDOWN_VALUE,
                    (millisUntilFinished + 1000) / MILLISECS_PER_SEC);
                mLocalBroadcastManager.sendBroadcast(mUpdateUIIntent);
            }

            @Override
            public void onFinish() {
                mLocalBroadcastManager.sendBroadcast(new Intent(
                    BROADCAST_ACTION_TIMER_FINISHED));

                startActivity(mTimerDialogIntent);
                stopSelf();
            }
        };

        // Display a notification so that the user knows the timer has started
        showNotification();

        // Start the countdown timer
        mCountDownTimer.start();
    }

    /**
     * This method displays a notification in the status bar, alerting the user
     * that the timer service has begun.
     *
     * This method also creates an Intent to the MainActivity and attaches it to
     * the notification. If the user clicks on the notification, the
     * MainActivity will be launched.
     */
    private void showNotification() {
        PendingIntent mContentIntent = PendingIntent.getActivity(this, 0,
            new Intent(this, MainActivity.class), 0);

        Notification mNotification = new Notification.Builder(this)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(getResources().getString(R.string.app_name))
            .setContentText("Timer is counting down")
            .setTicker("Timer has started")
            .setWhen(System.currentTimeMillis() + duration + 1000)
            .setUsesChronometer(true).setOngoing(true)
            .setContentIntent(mContentIntent).build();

        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }
}
