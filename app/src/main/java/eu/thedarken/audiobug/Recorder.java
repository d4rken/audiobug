package eu.thedarken.audiobug;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Recorder {
    private static final String TAG = "AB:Recorder";
    public static final String KEY_SAVE_LOCATION = "general.location";
    private final MediaRecorder mRecorder;
    private final File mRecordedFile;

    private static final String DEFAULT_SAVE_LOCATION = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Audio Bug/";

    private final RecorderCallback mListener;

    public Recorder(Context context, RecorderCallback listener) {
        Log.d(getClass().getSimpleName(), " created");
        this.mListener = listener;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        int bitRate = settings.getInt("recorder.bitrate", 160000);
        int samplingRate = settings.getInt("recorder.samplingrate", 44100);


        String _customPath = settings.getString(KEY_SAVE_LOCATION, DEFAULT_SAVE_LOCATION);
        File path = new File(_customPath);
        if (path.canWrite()) {
            if (!path.exists())
                path.mkdirs();
        } else {
            path = new File(DEFAULT_SAVE_LOCATION);
        }
        settings.edit().putString(KEY_SAVE_LOCATION, path.getAbsolutePath()).commit();

        Date d = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH#mm#ss");
        CharSequence s = format.format(d);
        String fileName = (s + ".mp4");

        mRecordedFile = new File(path, fileName);

        Log.d(TAG, "Recording will be stored in " + mRecordedFile.getAbsolutePath());
        mRecorder = new MediaRecorder();
        mRecorder.reset();

        mRecorder.setAudioChannels(1);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mRecorder.setAudioEncodingBitRate(bitRate);
        mRecorder.setAudioSamplingRate(samplingRate);
        Boolean codecError = false;
        try {
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        } catch (Exception e) {
            codecError = true;
        }
        if (codecError) {
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        }
        mRecorder.setOutputFile(mRecordedFile.getAbsolutePath());
        mRecorder.setAudioEncoder(3);
        try {
            mRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        Log.d(getClass().getSimpleName(), "recording now");
        mRecorder.start();
        mListener.onRecordingStarted();
    }

    public void stop() {
        if (mRecorder != null) {
            mListener.onRecordingFinished(mRecordedFile);
            mRecorder.reset();
            mRecorder.release();
        }
    }

    interface RecorderCallback {
        public void onRecordingFinished(File path);

        public void onRecordingStarted();
    }

}
