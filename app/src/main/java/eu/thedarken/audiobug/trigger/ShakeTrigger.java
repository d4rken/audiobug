package eu.thedarken.audiobug.trigger;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;


public class ShakeTrigger extends Trigger implements SensorEventListener {
    private static final String TAG = "AB:ShakeTrigger";
    public static final String KEY_SHAKE_THRESHOLD = "trigger.shake.threshold";
    public static final String KEY_SHAKE_COUNT = "trigger.shake.count";
    public static final String KEY_SHAKE_TIMEOUT = "trigger.shake.timeout";
    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    private final int mShakeThreshold;
    private final int mShakeCount;
    private final int mShakeTimeout;
    private long mLastUpdate = -1;
    private long mLastShake = -1;
    private float mLastX, mLastY, mLastZ;

    private long mLastTriggered = -1;

    private int mShakes = 0;

    public ShakeTrigger(Context context, TriggerListener listener) {
        super(context, listener);

        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        mShakeThreshold = settings.getInt(KEY_SHAKE_THRESHOLD, 1000);
        mShakeCount = settings.getInt(KEY_SHAKE_COUNT, 3);
        mShakeTimeout = settings.getInt(KEY_SHAKE_TIMEOUT, 300);
    }

    @Override
    public void start() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void stop() {
        mSensorManager.unregisterListener(this, mAccelerometer);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent arg0) {
        if (arg0.sensor == mAccelerometer) {
            final long currentTime = System.currentTimeMillis();

            long diffTime = (currentTime - mLastUpdate);
            mLastUpdate = currentTime;

            float x = arg0.values[0];
            float y = arg0.values[1];
            float z = arg0.values[2];

            float mForce = Math.abs((x + y + z - mLastX - mLastY - mLastZ) / diffTime * 10000);

            if (mForce > mShakeThreshold) {
                if ((currentTime - mLastShake) > mShakeTimeout) {
                    mShakes = 0;
                }
                mShakes++;
                mLastShake = currentTime;

                if (((currentTime - mLastTriggered) > 3000) && (mShakes >= mShakeCount)) {
                    mLastTriggered = currentTime;
                    mShakes = 0;
                    Log.d(TAG, "TRIGGERED!");
                    getListener().onTriggered();
                }
            }
            mLastX = x;
            mLastY = y;
            mLastZ = z;
        }
    }
}
