package eu.thedarken.audiobug.trigger;

import android.content.Context;

public abstract class Trigger {
    public static final String KEY_TRIGGER_TYPE = "general.trigger";
    private final TriggerListener mListener;
    private final Context mContext;

    public enum TYPE {
        NONE, SHAKE, PROXIMITY
    }

    public Trigger(Context context, TriggerListener mListener) {
        this.mContext = context;
        this.mListener = mListener;
    }

    public abstract void start();

    public abstract void stop();

    public TriggerListener getListener() {
        return mListener;
    }

    protected Context getContext() {
        return mContext;
    }


    public interface TriggerListener {
        public void onTriggered();
    }

}
