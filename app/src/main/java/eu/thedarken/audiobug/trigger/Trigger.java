package eu.thedarken.audiobug.trigger;

import android.content.Context;

public abstract class Trigger {
    public static final String KEY_TRIGGER_TYPE = "general.trigger";
    private final TriggerListener mListener;
    private final Context mContext;

    public enum TYPE {
        NONE, SHAKE, PROXIMITY
    }

    Trigger(Context context, TriggerListener mListener) {
        this.mContext = context;
        this.mListener = mListener;
    }

    public abstract void start();

    public abstract void stop();

    TriggerListener getListener() {
        return mListener;
    }

    Context getContext() {
        return mContext;
    }


    public interface TriggerListener {
        void onTriggered();
    }

}
