package com.pivotal.cf.mobile.datasdk.sample.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.DataSDK;
import com.pivotal.cf.mobile.datasdk.activity.BaseAuthorizationActivity;
import com.pivotal.cf.mobile.datasdk.sample.R;
import com.pivotal.cf.mobile.datasdk.sample.util.Preferences;

import java.net.MalformedURLException;
import java.net.URL;

public class AuthorizationActivity extends BaseAuthorizationActivity {

    private DataSDK dataSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);
        dataSDK = DataSDK.getInstance();
    }

    @Override
    public void authorizationComplete() {
        finish();
    }

    @Override
    public void authorizationFailed(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupButtons();
    }

    private void setupButtons() {
        final Button authorizeButton = (Button) findViewById(R.id.action_authorize);
        authorizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtainAuthorization();
            }
        });
    }

    private void obtainAuthorization() {
        dataSDK.obtainAuthorization(this, getDataParameters());
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
