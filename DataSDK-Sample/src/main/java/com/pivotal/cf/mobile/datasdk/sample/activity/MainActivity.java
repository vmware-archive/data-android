package com.pivotal.cf.mobile.datasdk.sample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.pivotal.cf.mobile.common.sample.activity.BaseMainActivity;
import com.pivotal.cf.mobile.common.sample.activity.BasePreferencesActivity;
import com.pivotal.cf.mobile.datasdk.DataSDK;
import com.pivotal.cf.mobile.datasdk.sample.R;

public class MainActivity extends BaseMainActivity {

    private DataSDK dataSDK;

    protected Class<? extends BasePreferencesActivity> getPreferencesActivity() {
        return PreferencesActivity.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (logItems.isEmpty()) {
            addLogMessage("Press the \"Authorize\" button to obtain authorization.");
        }
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentBaseRowColour();
        dataSDK = DataSDK.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_authorize:
                doAuthorize();
                break;

            case R.id.action_clear_authorization:
                doClearAuthorization();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void doAuthorize() {
        final Intent intent = new Intent(this, AuthorizationActivity.class);
        startActivity(intent);
    }

    private void doClearAuthorization() {
        // TODO - implement

    }

}
