package eu.thedarken.audiobug.trigger;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class ProximityTrigger extends Trigger implements SensorEventListener {
    private static final String TAG = "AB:ProximityTrigger";
    public static final String KEY_PROXIMITY_TIMEOUT = "trigger.proximity.timeout";
    public static final String KEY_PROXIMITY_COUNT = "trigger.proximity.count";
    private final SensorManager mSensorManager;
    private final Sensor mProximitySensor;
    private final int mProximityTimeout;
    private final int mProximityCount;

    private long mLastTap = -1;
    private long mTaps = 0;
    private long mLastTrigger = -1;

    public ProximityTrigger(Context context, TriggerListener listener) {
        super(context, listener);
        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        mProximityTimeout = settings.getInt(KEY_PROXIMITY_TIMEOUT, 500);
        mProximityCount = settings.getInt(KEY_PROXIMITY_COUNT, 3);

        Log.d(TAG, "created with timeout " + mProximityTimeout + " and count " + mProximityCount);
    }

    @Override
    public void start() {
        mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent se) {
        if (se.sensor == mProximitySensor) {
            if (se.values[0] == 0) {
                long cur = System.currentTimeMillis();
                if ((cur - mLastTap) > mProximityTimeout) {
                    mTaps = 0;
                }
                mTaps++;
                if (mTaps >= mProximityCount && ((cur - mLastTrigger) > 2000)) {
                    mTaps = 0;
                    Log.d(TAG, "TRIGGERED!");
                    mLastTrigger = cur;
                    getListener().onTriggered();
                }
                mLastTap = cur;
            }
        }
    }


}
