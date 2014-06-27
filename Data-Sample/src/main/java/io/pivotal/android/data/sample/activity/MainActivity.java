package io.pivotal.android.data.sample.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import io.pivotal.android.common.sample.activity.BaseMainActivity;
import io.pivotal.android.common.sample.activity.BasePreferencesActivity;
import io.pivotal.android.common.util.Logger;
import io.pivotal.android.data.DataParameters;
import io.pivotal.android.data.DataSDK;
import io.pivotal.android.data.client.AuthorizedResourceClientImpl;
import io.pivotal.android.data.sample.util.Preferences;
import io.pivotal.android.data.util.StreamUtil;
import io.pivotal.data.android.sample.R;

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
        try {
            dataSDK.setParameters(this, getDataParameters());
        } catch (Exception e) {
            addLogMessage("Could not set parameters: " + e.getLocalizedMessage());
        }
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

            case R.id.action_get_user_info:
                doGetUserInfo();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void doAuthorize() {
        try {
            dataSDK.obtainAuthorization(this);
        } catch (Exception e) {
            addLogMessage("Could not obtain authorization: '" + e + "'.");
        }
    }

    private void doClearAuthorization() {
        try {
            dataSDK.clearAuthorization(this);
        } catch (Exception e) {
            addLogMessage("Could not clear authorization: '" + e.getLocalizedMessage() + "'.");
        }
    }

    private void doGetUserInfo() {
        final URL userInfoUrl = getUserInfoUrl();
        final DataParameters parameters = getDataParameters();
        try {
            dataSDK.getClient(this).executeHttpRequest("GET", userInfoUrl, null, "", "", null, new AuthorizedResourceClientImpl.Listener() {

                @Override
                public void onSuccess(int httpStatusCode, String contentType, String contentEncoding, InputStream result) {
                    Logger.d("GET userInfo onSuccess");
                    if (httpStatusCode >= 200 && httpStatusCode < 300) {
                        if (contentType.startsWith("application/json")) {
                            try {
                                final String responseData = StreamUtil.readInput(result);
                                Logger.d("Read user info data: " + responseData);
                                result.close();
                            } catch (IOException e) {
                                Logger.ex("Could not read user info response data", e);
                            }
                        } else {
                            Logger.e("Got invalid content type: " + contentType + ".");
                        }
                    } else {
                        Logger.e("Got error HTTP status getting user info: '" + httpStatusCode + ".");
                    }
                }

                @Override
                public void onUnauthorized() {
                    Logger.e("GET failed. Not authorized.");
                }

                @Override
                public void onFailure(String reason) {
                    Logger.e("GET userInfo onFailure reason: '" + reason + "'.");
                }
            });
        } catch (Exception e) {
            Logger.e("Could not prepare GET userInfo request. Reason: '" + e.getLocalizedMessage() + "'.");
        }
    }

    private DataParameters getDataParameters() {

        final URL authorizationUrl = getAuthorizationUrl();
        final URL tokenUrl = getTokenUrl();
        final String redirectUrl = getRedirectUrl();
        final URL dataServicesUrl = getDataServicesUrl();

        return new DataParameters(
                Preferences.getClientId(this),
                Preferences.getClientSecret(this),
                authorizationUrl,
                tokenUrl,
                redirectUrl,
                dataServicesUrl);
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

    private URL getDataServicesUrl() {
        try {
            return new URL(Preferences.getDataServicesUrl(this));
        } catch (MalformedURLException e) {
            Logger.e("Invalid data services URL: " + e.getLocalizedMessage());
            return null;
        }
    }

    private String getRedirectUrl() {
        return Preferences.getRedirectUrl(this);
    }

    private URL getUserInfoUrl() {
        try {
            return new URL(Preferences.getUserInfoUrl(this));
        } catch (MalformedURLException e) {
            Logger.e("Invalid user info URL: " + e.getLocalizedMessage());
            return null;
        }
    }
}
