package com.pivotal.cf.mobile.datasdk.sample.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.pivotal.cf.mobile.common.sample.activity.BaseMainActivity;
import com.pivotal.cf.mobile.common.sample.activity.BasePreferencesActivity;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.DataSDK;
import com.pivotal.cf.mobile.datasdk.sample.R;
import com.pivotal.cf.mobile.datasdk.sample.util.Preferences;

import java.net.MalformedURLException;
import java.net.URL;

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
        dataSDK.obtainAuthorization(this, getDataParameters());
    }

    private void doClearAuthorization() {
        // TODO - implement

    }

    private DataParameters getDataParameters() {

        final URL authorizationUrl = getAuthorizationUrl();
        final URL tokenUrl = getTokenUrl();
        final URL userInfoUrl = getUserInfoUrl();
        final URL redirectUrl = getRedirectUrl();

        return new DataParameters(
                Preferences.getClientId(this),
                Preferences.getClientSecret(this),
                authorizationUrl,
                tokenUrl,
                userInfoUrl,
                redirectUrl);
    }

    private URL getAuthorizationUrl() {
        try {
            return new URL(Preferences.getAuthorizationUrl(this));
        } catch (MalformedURLException e) {
            Logger.e("Invalid authorization URL: " + e.getLocalizedMessage());
            return null;
        }
    }

    private URL getTokenUrl() {
        try {
            return new URL(Preferences.getTokenUrl(this));
        } catch (MalformedURLException e) {
            Logger.e("Invalid token URL: " + e.getLocalizedMessage());
            return null;
        }
    }

    private URL getUserInfoUrl() {
        try {
            return new URL(Preferences.getUserInfoUrl(this));
        } catch (MalformedURLException e) {
            Logger.e("Invalid user info URL: " + e.getLocalizedMessage());
            return null;
        }
    }

    private URL getRedirectUrl() {
        try {
            return new URL(Preferences.getRedirectUrl(this));
        } catch (MalformedURLException e) {
            Logger.e("Invalid redirect URL: " + e.getLocalizedMessage());
            return null;
        }
    }
}
