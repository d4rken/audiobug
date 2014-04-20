package eu.thedarken.audiobug;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class PreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getString(Recorder.KEY_SAVE_LOCATION, null) == null)
            settings.edit().putString(Recorder.KEY_SAVE_LOCATION, Environment.getExternalStorageDirectory().getAbsolutePath() + "/Audio Bug/").commit();

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals("misc.github")) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/d4rken/audiobug"));
            startActivity(browserIntent);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
