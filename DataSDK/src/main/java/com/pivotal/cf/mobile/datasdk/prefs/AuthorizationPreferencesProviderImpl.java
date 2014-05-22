package com.pivotal.cf.mobile.datasdk.prefs;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.pivotal.cf.mobile.common.util.Logger;

import java.net.MalformedURLException;
import java.net.URL;

public class AuthorizationPreferencesProviderImpl implements AuthorizationPreferencesProvider {

    public static final String TAG_NAME = "PivotalCFMSDataSDK";

    private static final String PROPERTY_CLIENT_ID = "client_id";
    private static final String PROPERTY_CLIENT_SECRET = "client_secret";
    private static final String PROPERTY_AUTHORIZATION_URL = "authorization_url";
    private static final String PROPERTY_TOKEN_URL = "token_url";
    private static final String PROPERTY_REDIRECT_URL = "redirect_url";

    private Context context;

    public AuthorizationPreferencesProviderImpl(Context context) {
        verifyArguments(context);
        saveArguments(context);
    }

    private void saveArguments(Context context) {
        if (!(context instanceof Application)) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }
    }

    private void verifyArguments(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
    }

    @Override
    public String getClientId() {
        return getSharedPreferences().getString(PROPERTY_CLIENT_ID, null);
    }

    @Override
    public void setClientId(String clientId) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_CLIENT_ID, clientId);
        editor.commit();

    }

    @Override
    public String getClientSecret() {
        return getSharedPreferences().getString(PROPERTY_CLIENT_SECRET, null);
    }

    @Override
    public void setClientSecret(String clientSecret) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_CLIENT_SECRET, clientSecret);
        editor.commit();
    }

    @Override
    public URL getAuthorizationUrl() {
        final String preference = getSharedPreferences().getString(PROPERTY_AUTHORIZATION_URL, null);
        if (preference == null) {
            return null;
        }
        try {
            return new URL(preference);
        } catch (MalformedURLException e) {
            Logger.w("Invalid authorization URL stored in preferences: " + preference);
            return null;
        }
    }

    @Override
    public void setAuthorizationUrl(URL authorizationUrl) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        if (authorizationUrl != null) {
            editor.putString(PROPERTY_AUTHORIZATION_URL, authorizationUrl.toString());
        } else {
            editor.putString(PROPERTY_AUTHORIZATION_URL, null);
        }
        editor.commit();
    }

    @Override
    public URL getTokenUrl() {
        final String preference = getSharedPreferences().getString(PROPERTY_TOKEN_URL, null);
        if (preference == null) {
            return null;
        }
        try {
            return new URL(preference);
        } catch (MalformedURLException e) {
            Logger.w("Invalid token URL stored in preferences: " + preference);
            return null;
        }
    }

    @Override
    public void setTokenUrl(URL tokenUrl) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        if (tokenUrl != null) {
            editor.putString(PROPERTY_TOKEN_URL, tokenUrl.toString());
        } else {
            editor.putString(PROPERTY_TOKEN_URL, null);
        }
        editor.commit();
    }

    @Override
    public URL getRedirectUrl() {
        final String preference = getSharedPreferences().getString(PROPERTY_REDIRECT_URL, null);
        if (preference == null) {
            return null;
        }
        try {
            return new URL(preference);
        } catch (MalformedURLException e) {
            Logger.w("Invalid redirect URL stored in preferences: " + preference);
            return null;
        }
    }

    @Override
    public void setRedirectUrl(URL redirectUrl) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        if (redirectUrl != null) {
            editor.putString(PROPERTY_REDIRECT_URL, redirectUrl.toString());
        } else {
            editor.putString(PROPERTY_REDIRECT_URL, null);
        }
        editor.commit();
    }

    public void clear() {
        getSharedPreferences().edit().clear().commit();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(TAG_NAME, Context.MODE_PRIVATE);
    }
}
