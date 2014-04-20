package eu.thedarken.audiobug;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

import eu.thedarken.audiobug.trigger.ProximityTrigger;
import eu.thedarken.audiobug.trigger.ShakeTrigger;
import eu.thedarken.audiobug.trigger.Trigger;

public class ConfigurationFragment extends Fragment implements ServiceConnection, Recorder.RecorderCallback {
    private SharedPreferences mSettings;
    private RadioButton rbProximity, rbShake, rbNone;
    private LinearLayout layoutProximity, layoutShake;
    private LinearLayout layoutRunningParent;
    private View layoutConfigParent;
    private Button recorderToggleButton;
    private Trigger.TYPE mTriggerType;
    private Intent mService;
    private AudioBugService.LocalBinder mBinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mService = new Intent(getActivity(), AudioBugService.class);
        mTriggerType = Trigger.TYPE.valueOf(mSettings.getString(Trigger.KEY_TRIGGER_TYPE, Trigger.TYPE.NONE.name()));
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.configuration_fragment_layout, container, false);

        recorderToggleButton = (Button) layout.findViewById(R.id.bt_toggle_recorder);
        recorderToggleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBinder != null)
                    mBinder.toggleRecorder();
            }
        });

        layoutConfigParent = layout.findViewById(R.id.rg_configgroup);
        layoutConfigParent.setEnabled(false);
        layoutRunningParent = (LinearLayout) layout.findViewById(R.id.ll_runninglayout);
        layoutRunningParent.setVisibility(View.GONE);

        layoutProximity = (LinearLayout) layout.findViewById(R.id.ll_proximity_layout);
        layoutShake = (LinearLayout) layout.findViewById(R.id.ll_shake_layout);

        rbNone = (RadioButton) layout.findViewById(R.id.rb_none);
        rbProximity = (RadioButton) layout.findViewById(R.id.rb_proximity);
        rbShake = (RadioButton) layout.findViewById(R.id.rb_shake);
        rbNone.setOnClickListener(rlNone);
        rbProximity.setOnClickListener(rlProximity);
        rbShake.setOnClickListener(rlShake);

        final SeekBar sbShakeThreshold = (SeekBar) layout.findViewById(R.id.sb_shake_threshold);
        final TextView tvShakeThreshold = (TextView) layout.findViewById(R.id.tv_shake_threshold);
        sbShakeThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                mSettings.edit().putInt(ShakeTrigger.KEY_SHAKE_THRESHOLD, arg0.getProgress()).commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                tvShakeThreshold.setText(getString(R.string.shake_slider_threshold, arg1));
            }
        });
        int shakeThreshold = mSettings.getInt(ShakeTrigger.KEY_SHAKE_THRESHOLD, 1000);
        sbShakeThreshold.setProgress(shakeThreshold);

        final SeekBar sbShakeCount = (SeekBar) layout.findViewById(R.id.sb_shake_count);
        final TextView tvShakeCount = (TextView) layout.findViewById(R.id.tv_shake_count);
        sbShakeCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                mSettings.edit().putInt(ShakeTrigger.KEY_SHAKE_COUNT, arg0.getProgress()).commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                tvShakeCount.setText(getString(R.string.shake_slider_count, arg1));
            }
        });
        int shakeCount = mSettings.getInt(ShakeTrigger.KEY_SHAKE_COUNT, 3);
        sbShakeCount.setProgress(shakeCount);

        final SeekBar sbShakeTimeout = (SeekBar) layout.findViewById(R.id.sb_SHAKE_TIMEOUT);
        final TextView tvShakeTimeout = (TextView) layout.findViewById(R.id.tv_shake_timeout);
        sbShakeTimeout.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                mSettings.edit().putInt(ShakeTrigger.KEY_SHAKE_TIMEOUT, arg0.getProgress()).commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                tvShakeTimeout.setText(getString(R.string.shake_slider_timeout, arg1));
            }
        });
        int shakeTimeout = mSettings.getInt(ShakeTrigger.KEY_SHAKE_TIMEOUT, 300);
        sbShakeTimeout.setProgress(shakeTimeout);

        final SeekBar sbProximityCount = (SeekBar) layout.findViewById(R.id.sb_proximity_count);
        final TextView tvProximityCount = (TextView) layout.findViewById(R.id.tv_proximity_count);
        sbProximityCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                mSettings.edit().putInt(ProximityTrigger.KEY_PROXIMITY_COUNT, arg0.getProgress()).commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                tvProximityCount.setText(getString(R.string.proximity_slider_taps, arg1));
            }
        });
        int proximityCount = mSettings.getInt(ProximityTrigger.KEY_PROXIMITY_COUNT, 3);
        sbProximityCount.setProgress(proximityCount);

        final SeekBar sbProximityTimeout = (SeekBar) layout.findViewById(R.id.sb_proximity_timeout);
        final TextView tvProximityTimeout = (TextView) layout.findViewById(R.id.tv_proximity_timeout);
        sbProximityTimeout.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                mSettings.edit().putInt(ProximityTrigger.KEY_PROXIMITY_TIMEOUT, arg0.getProgress()).commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                tvProximityTimeout.setText(getString(R.string.proximity_slider_timeout, arg1));
            }
        });
        int proximityTimeout = mSettings.getInt(ProximityTrigger.KEY_PROXIMITY_TIMEOUT, 500);
        sbProximityTimeout.setProgress(proximityTimeout);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        updateUI(mTriggerType);
        setHasOptionsMenu(true);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        getActivity().bindService(mService, this, 0);
        super.onResume();
    }

    @Override
    public void onPause() {
        getActivity().unbindService(this);
        super.onPause();
    }

    private final OnClickListener rlNone = new OnClickListener() {
        public void onClick(View v) {
            updateTrigger(Trigger.TYPE.NONE);
        }
    };
    private final OnClickListener rlProximity = new OnClickListener() {
        public void onClick(View v) {
            updateTrigger(Trigger.TYPE.PROXIMITY);
        }
    };
    private final OnClickListener rlShake = new OnClickListener() {
        public void onClick(View v) {
            updateTrigger(Trigger.TYPE.SHAKE);
        }
    };

    private void updateTrigger(Trigger.TYPE type) {
        mTriggerType = Trigger.TYPE.SHAKE;
        SharedPreferences.Editor prefEditor = mSettings.edit();
        prefEditor.putString(Trigger.KEY_TRIGGER_TYPE, type.name()).commit();
        updateUI(type);
    }

    private void updateUI(Trigger.TYPE trigger) {
        if (trigger == Trigger.TYPE.NONE) {
            rbNone.setChecked(true);
            layoutProximity.setVisibility(View.GONE);
            layoutShake.setVisibility(View.GONE);
        } else if (trigger == Trigger.TYPE.PROXIMITY) {
            rbProximity.setChecked(true);
            layoutProximity.setVisibility(View.VISIBLE);
            layoutShake.setVisibility(View.GONE);
        } else if (trigger == Trigger.TYPE.SHAKE) {
            rbShake.setChecked(true);
            layoutShake.setVisibility(View.VISIBLE);
            layoutProximity.setVisibility(View.GONE);
        }
    }

    private void start() {
        getActivity().startService(mService);
        getActivity().bindService(mService, this, 0);
    }

    private void stop() {
        getActivity().stopService(mService);
    }

    private void switchLayout(boolean recorder) {
        if (recorder) {
            layoutConfigParent.setEnabled(false);
            layoutConfigParent.setVisibility(View.GONE);
            layoutRunningParent.setVisibility(View.VISIBLE);
            if (mBinder != null && mBinder.isRecording()) {
                recorderToggleButton.setText("Stop recording");
            } else {
                recorderToggleButton.setText("Start recording");
            }
        } else {
            layoutConfigParent.setEnabled(true);
            layoutConfigParent.setVisibility(View.VISIBLE);
            layoutRunningParent.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.service_menu, menu);
        MenuItem toggle = menu.findItem(R.id.start);
        if (toggle != null) {
            if (mBinder == null) {
                toggle.setTitle("Start");
                toggle.setIcon(R.drawable.ic_action_playback_play);
            } else {
                toggle.setTitle("Stop");
                toggle.setIcon(R.drawable.ic_action_playback_stop);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d("binder", "1");
        mBinder = (AudioBugService.LocalBinder) iBinder;
        ActionBar bar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.service_running)));
        switchLayout(true);
        mBinder.setRecorderListener(this);
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d("binder", "0");

        mBinder = null;
        switchLayout(false);
        ActionBar bar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.abc_ab_transparent_dark_holo));
        bar.setSplitBackgroundDrawable(getResources().getDrawable(R.drawable.abc_ab_bottom_transparent_dark_holo));
        bar.setStackedBackgroundDrawable(getResources().getDrawable(R.drawable.abc_ab_stacked_transparent_dark_holo));
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onRecordingFinished(File path) {
        switchLayout(true);
        ActionBar bar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.service_running)));
    }

    @Override
    public void onRecordingStarted() {
        switchLayout(true);
        ActionBar bar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.service_recording)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start:
                if (mBinder == null) {
                    start();
                } else {
                    stop();
                }
                getActivity().supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
