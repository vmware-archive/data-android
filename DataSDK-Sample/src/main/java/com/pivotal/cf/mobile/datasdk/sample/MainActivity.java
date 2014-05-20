package com.pivotal.cf.mobile.datasdk.sample;

import android.os.Bundle;

import com.pivotal.cf.mobile.common.sample.activity.BaseMainActivity;
import com.pivotal.cf.mobile.common.sample.activity.BasePreferencesActivity;

public class MainActivity extends BaseMainActivity {

    protected Class<? extends BasePreferencesActivity> getPreferencesActivity() {
        return null /*PreferencesActivity.class*/;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (logItems.isEmpty()) {
            addLogMessage("Press the \"Login\" button to login (not implemented).");
        }
//        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentBaseRowColour();
    }

}
