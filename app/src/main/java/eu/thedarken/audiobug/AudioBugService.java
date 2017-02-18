package eu.thedarken.audiobug;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;

import eu.thedarken.audiobug.trigger.ProximityTrigger;
import eu.thedarken.audiobug.trigger.ShakeTrigger;
import eu.thedarken.audiobug.trigger.Trigger;

public class AudioBugService extends Service implements Recorder.RecorderCallback, Trigger.TriggerListener {
    private static final String TAG = "AB:ABService";
    private static final long[] VIB_START_PATTERN = {0, 300};
    private static final long[] VIB_STOP_PATTERN = {150, 300, 150, 300, 150, 300, 150};
    private static final int NOTIFICATION_SERVICE_ID = 1;
    private static final int NOTIFICATION_RECORDER_ID = 2;

    private Trigger mTrigger;
    private PowerManager.WakeLock mWakeLock;
    private Recorder mRecorder;
    private Trigger.TYPE mTriggerType = Trigger.TYPE.NONE;
    private long mLastWidgetPress = 0;
    private WidgetReceiver mReceiver;
    private Vibrator mVibrator;
    private boolean mShowServiceNotification = true;
    private boolean mShowRecorderNotification = true;
    private Recorder.RecorderCallback mRecorderListener;
    private Binder mBinder;

    public class LocalBinder extends Binder {
        boolean isRecording() {
            return mRecorder != null;
        }

        void toggleRecorder() {
            toggleRecording();
        }

        void setRecorderListener(Recorder.RecorderCallback callback) {
            if (callback != null && isRecording())
                callback.onRecordingStarted();

            mRecorderListener = callback;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);


        mReceiver = new WidgetReceiver();

        IntentFilter filter = new IntentFilter(RecorderWidget.ACTION_TOGGLE_RECORDING);
        registerReceiver(mReceiver, filter);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        mTriggerType = Trigger.TYPE.valueOf(settings.getString("general.trigger", Trigger.TYPE.NONE.name()));
        mShowServiceNotification = settings.getBoolean("general.notification.service", true);
        mShowRecorderNotification = settings.getBoolean("general.notification.recorder", true);
        boolean useWakelock = settings.getBoolean("general.wakelock", false);
        if (useWakelock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AudioBug");
            mWakeLock.acquire();
            Log.d(TAG, "Wakelock aquired");
        }

        setupTriggers();

        if (mShowServiceNotification) {
            setNotification(false);
        }
        Log.d(TAG, "ABService running");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(RecorderWidget.ACTION_TOGGLE_RECORDING)) {
                toggleRecording();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        stopRecording();
        changeWidgetImage(R.drawable.widgetnorm);
        unregisterReceiver(mReceiver);
        tearDownTriggers();
        if (mWakeLock != null) {
            mWakeLock.release();
            Log.d(TAG, "Wakelock released");
        }
        Log.d(TAG, "ABService destroyed");
        cancelNotifications();
        super.onDestroy();
    }

    @Override
    public void onRecordingFinished(File path) {
        mRecorder = null;
        if (mShowRecorderNotification) {
            Toast.makeText(this, getString(R.string.recording_saved_to, path), Toast.LENGTH_SHORT).show();
        }
        mVibrator.vibrate(VIB_STOP_PATTERN, -1);
        if (mShowServiceNotification) {
            setNotification(false);
        } else {
            cancelNotifications();
        }
        changeWidgetImage(R.drawable.widgetnorm);
        if (mRecorderListener != null)
            mRecorderListener.onRecordingFinished(path);
    }

    @Override
    public void onRecordingStarted() {
        if (mShowRecorderNotification) {
            Toast.makeText(this, getString(R.string.beginning_recording), Toast.LENGTH_SHORT).show();
        }
        mVibrator.vibrate(VIB_START_PATTERN, -1);
        if (mShowRecorderNotification)
            setNotification(true);
        changeWidgetImage(R.drawable.widgetrec);
        if (mRecorderListener != null)
            mRecorderListener.onRecordingStarted();
    }

    private void setupTriggers() {
        if (mTriggerType == Trigger.TYPE.SHAKE) {
            mTrigger = new ShakeTrigger(this, this);
            mTrigger.start();
        } else if (mTriggerType == Trigger.TYPE.PROXIMITY) {
            mTrigger = new ProximityTrigger(this, this);
            mTrigger.start();
        }
    }

    private void tearDownTriggers() {
        if (mTriggerType == Trigger.TYPE.SHAKE) {
            mTrigger.stop();
            mTrigger = null;
        } else if (mTriggerType == Trigger.TYPE.PROXIMITY) {
            mTrigger.stop();
            mTrigger = null;
        }
    }

    private void changeWidgetImage(int imgRes) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_layout);
        views.setImageViewResource(R.id.ib_widgetbutton, imgRes);
        Intent intent = new Intent(this, RecorderWidget.class);
        intent.setAction(RecorderWidget.ACTION_TOGGLE_RECORDING);
        PendingIntent myPI = PendingIntent.getBroadcast(this, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.ib_widgetbutton, myPI);
        ComponentName componentName = new ComponentName(this, RecorderWidget.class);
        AppWidgetManager.getInstance(this).updateAppWidget(componentName, views);
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null)
            mBinder = new LocalBinder();
        return mBinder;
    }

    private void toggleRecording() {
        if (mRecorder == null) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        if (mRecorder != null)
            stopRecording();
        mRecorder = new Recorder(AudioBugService.this, AudioBugService.this);
        mRecorder.start();
    }

    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
        }
    }

    @Override
    public void onTriggered() {
        toggleRecording();
    }

    private class WidgetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RecorderWidget.ACTION_TOGGLE_RECORDING) && ((System.currentTimeMillis() - mLastWidgetPress) > 1000)) {
                toggleRecording();
            }
            mLastWidgetPress = System.currentTimeMillis();
        }
    }

    private void setNotification(boolean recording) {
        int icon;
        String tickerText;
        String contentTitle;
        String contentText;
        if (recording) {
            icon = R.drawable.widgetrec;
            tickerText = "Beginning recording";
            contentTitle = "AudioBug";
            contentText = "RECORDING...";
        } else {
            icon = R.drawable.widgetnorm;
            tickerText = "Waiting for action";
            contentTitle = "AudioBug";
            contentText = "IDLE";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setTicker(tickerText);
        builder.setSmallIcon(icon);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Notification.FLAG_ONGOING_EVENT);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(contentIntent);
        builder.setContentTitle(contentTitle);
        builder.setContentText(contentText);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_SERVICE_ID, builder.build());

    }

    private void cancelNotifications() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_SERVICE_ID);
        mNotificationManager.cancel(NOTIFICATION_RECORDER_ID);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("unbind", "ding");
        mRecorderListener = null;
        return super.onUnbind(intent);
    }
}
