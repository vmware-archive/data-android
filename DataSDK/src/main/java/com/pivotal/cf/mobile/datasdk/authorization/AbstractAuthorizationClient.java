package com.pivotal.cf.mobile.datasdk.authorization;

import android.app.Application;
import android.content.Context;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class AbstractAuthorizationClient {

    // TODO - this item should be provided via the constructor (i.e.: dependency injection)
    protected static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    protected static final JsonFactory JSON_FACTORY = new JacksonFactory();
    protected static FileDataStoreFactory dataStoreFactory;

    // TODO - these scopes will likely be different in the final product
    protected static final String[] SCOPES = new String[] {"profile", "email"};

    protected AuthorizationPreferencesProvider authorizationPreferencesProvider;
    protected Context context;

    public AbstractAuthorizationClient(Context context, AuthorizationPreferencesProvider authorizationPreferencesProvider) {
        verifyArguments(context, authorizationPreferencesProvider);
        saveArguments(context, authorizationPreferencesProvider);
        setupDataStore(context);
    }

    private void verifyArguments(Context context, AuthorizationPreferencesProvider authorizationPreferencesProvider) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (authorizationPreferencesProvider == null) {
            throw new IllegalArgumentException("authorizationPreferencesProvider may not be null");
        }
    }

    private void saveArguments(Context context, AuthorizationPreferencesProvider authorizationPreferencesProvider) {
        if (!(context instanceof Application)) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }
        this.authorizationPreferencesProvider = authorizationPreferencesProvider;
    }

    private void setupDataStore(Context context) {
        final File dataStoreDir = context.getDir("oauth2", Context.MODE_PRIVATE);
        try {
            dataStoreFactory = new FileDataStoreFactory(dataStoreDir);
        } catch (IOException e) {
            // TODO - pass errors back via callback
            Logger.ex("Could not open file data store", e);
        }
    }

    protected AuthorizationCodeFlow getFlow() {

        try {
            final String clientId = authorizationPreferencesProvider.getClientId();
            final String clientSecret = authorizationPreferencesProvider.getClientSecret();
            final String authorizationURL = authorizationPreferencesProvider.getAuthorizationUrl().toString();
            final URL tokenUrl = authorizationPreferencesProvider.getTokenUrl();

            return new AuthorizationCodeFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    HTTP_TRANSPORT,
                    JSON_FACTORY,
                    new GenericUrl(tokenUrl),
                    new ClientParametersAuthentication(clientId, clientSecret),
                    clientId,
                    authorizationURL)
                    .setScopes(Arrays.asList(SCOPES))
                    .setDataStoreFactory(dataStoreFactory).build();

        } catch (IOException e) {
            // TODO - pass errors back via callback
            Logger.ex("Could not create AuthorizationCodeFlow object", e);
            return null;
        }
    }

    protected void storeTokenResponse(AuthorizationCodeFlow flow, TokenResponse tokenResponse) {
        try {
            // TODO - make a new parameter for the user ID.
            flow.createAndStoreCredential(tokenResponse, authorizationPreferencesProvider.getClientId());
        } catch (IOException e) {
            Logger.ex("Could not store token response", e);
        }
    }

    protected Credential loadCredentials(AuthorizationCodeFlow flow) {
        try {
            // TODO - make a new parameter for the user ID.
            final Credential credential = flow.loadCredential(authorizationPreferencesProvider.getClientId());
            return credential;
        } catch (IOException e) {
            Logger.ex("Could not load user credentials", e);
            return null;
        }
    }
}
