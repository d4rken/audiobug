package eu.thedarken.audiobug;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {
    private Fragment mContentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);

        if (savedInstanceState == null) {
            mContentFragment = new ConfigurationFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fl_contentframe, mContentFragment);
            fragmentTransaction.commit();
        } else {
            mContentFragment = getSupportFragmentManager().findFragmentById(R.id.fl_contentframe);
        }

        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("general.info.shown", false)) {
            InfoDialogFragment infoDialog = InfoDialogFragment.newInstance();
            infoDialog.showDialog(this);
            sharedPreferences.edit().putBoolean("general.info.shown", true).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent startPreferencesActivity = new Intent(this, PreferencesActivity.class);
                startActivity(startPreferencesActivity);
                return true;
            case R.id.follow:
                FollowMeDialogFragment followDialog = FollowMeDialogFragment.newInstance();
                followDialog.showDialog(this);
                return true;
            case R.id.info:
                InfoDialogFragment infoDialog = InfoDialogFragment.newInstance();
                infoDialog.showDialog(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}