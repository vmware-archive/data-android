package com.pivotal.cf.mobile.datasdk.authorization;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.activity.BaseAuthorizationActivity;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class AuthorizationEngine {

    // TODO - these items should be provided via the constructor (i.e.: dependency injection)
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static FileDataStoreFactory dataStoreFactory;

    // TODO - these scopes will likely be different in the final product
    private static final String[] SCOPES = new String[] {"profile", "email"};

    // TODO - the state token should be randomly generated, but persisted until the end of the flow
    private static final String STATE_TOKEN = "BLORG";

    private AuthorizationCodeFlow flow;
    private AuthorizationPreferencesProvider authorizationPreferencesProvider;

    public AuthorizationEngine(AuthorizationPreferencesProvider authorizationPreferencesProvider) {
        verifyArguments(authorizationPreferencesProvider);
        saveArguments(authorizationPreferencesProvider);
    }

    private void verifyArguments(AuthorizationPreferencesProvider authorizationPreferencesProvider) {
        if (authorizationPreferencesProvider == null) {
            throw new IllegalArgumentException("authorizationPreferencesProvider may not be null");
        }
    }

    private void saveArguments(AuthorizationPreferencesProvider authorizationPreferencesProvider) {
        this.authorizationPreferencesProvider = authorizationPreferencesProvider;
    }

    public void obtainAuthorization(BaseAuthorizationActivity activity, DataParameters parameters) {
        verifyAuthorizationArguments(activity, parameters);
        saveAuthorizationParameters(parameters);
        setupDataStore(activity);
        setupFlow(parameters);
        startAuthorization(activity, parameters);
    }

    private void verifyAuthorizationArguments(BaseAuthorizationActivity activity, DataParameters parameters) {
        if (activity == null) {
            throw new IllegalArgumentException("activity may not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.getClientId() == null) {
            throw new IllegalArgumentException("parameters.clientId may not be null");
        }
        if (parameters.getClientSecret() == null) {
            throw new IllegalArgumentException("parameters.clientSecret may not be null");
        }
        if (parameters.getAuthorizationUrl() == null) {
            throw new IllegalArgumentException("parameters.authorizationUrl may not be null");
        }
        if (parameters.getTokenUrl() == null) {
            throw new IllegalArgumentException("parameters.tokenUrl may not be null");
        }
        if (parameters.getUserInfoUrl() == null) {
            throw new IllegalArgumentException("parameters.userInfoUrl may not be null");
        }
        if (parameters.getRedirectUrl() == null) {
            throw new IllegalArgumentException("parameters.redirectUrl may not be null");
        }
    }

    private void saveAuthorizationParameters(DataParameters parameters) {
        authorizationPreferencesProvider.setClientId(parameters.getClientId());
        authorizationPreferencesProvider.setClientSecret(parameters.getClientSecret());
        authorizationPreferencesProvider.setAuthorizationUrl(parameters.getAuthorizationUrl());
        authorizationPreferencesProvider.setTokenUrl(parameters.getTokenUrl());
        authorizationPreferencesProvider.setUserInfoUrl(parameters.getUserInfoUrl());
        authorizationPreferencesProvider.setRedirectUrl(parameters.getRedirectUrl());
    }

    private void startAuthorization(BaseAuthorizationActivity activity, DataParameters parameters) {
        final AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl();
        authorizationUrl.setRedirectUri(parameters.getRedirectUrl().toString());
        authorizationUrl.setState(STATE_TOKEN);
        final String url = authorizationUrl.build();
        Logger.d("Loading authorization request URL: " + url);
        final Uri uri = Uri.parse(url);
        final Intent i = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(i); // Launches external browser to do complete authentication
    }

    private void setupFlow(DataParameters parameters) {
        try {

            flow = new AuthorizationCodeFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    HTTP_TRANSPORT,
                    JSON_FACTORY,
                    new GenericUrl(parameters.getTokenUrl()),
                    new ClientParametersAuthentication(parameters.getClientId(), parameters.getClientSecret()),
                    parameters.getClientId(),
                    parameters.getAuthorizationUrl().toString())
                    .setScopes(Arrays.asList(SCOPES))
                    .setDataStoreFactory(dataStoreFactory).build();

        } catch (IOException e) {
            // TODO - pass errors back via callback
            Logger.ex("Could not create AuthorizationCodeFlow object", e);
        }
    }

    private void setupDataStore(BaseAuthorizationActivity activity) {
        final File dataStoreDir = activity.getDir("oauth2", Context.MODE_PRIVATE);
        try {
            dataStoreFactory = new FileDataStoreFactory(dataStoreDir);
        } catch (IOException e) {
            // TODO - pass errors back via callback
            Logger.ex("Could not open file data store", e);
        }
    }
}
