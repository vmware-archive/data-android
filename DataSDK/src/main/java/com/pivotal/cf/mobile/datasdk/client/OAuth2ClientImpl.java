package com.pivotal.cf.mobile.datasdk.client;

import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

public class OAuth2ClientImpl implements OAuth2Client {

    // TODO - this item should be provided via the constructor (i.e.: dependency injection)
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static FileDataStoreFactory dataStoreFactory;

    // TODO - these scopes will likely be different in the final product
    private static final String[] SCOPES = new String[] {"profile", "email"};

    private AuthorizationPreferencesProvider authorizationPreferencesProvider;

    public OAuth2ClientImpl(Context context, AuthorizationPreferencesProvider authorizationPreferencesProvider) {
        verifyArguments(context, authorizationPreferencesProvider);
        saveArguments(authorizationPreferencesProvider);
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

    private void saveArguments(AuthorizationPreferencesProvider authorizationPreferencesProvider) {
        this.authorizationPreferencesProvider = authorizationPreferencesProvider;
    }

    @Override
    public void get(final URL url, final Map<String, String> headers, DataParameters parameters, final Listener listener) {

        // TODO - user a thread pool to process request

        final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                Logger.fd("Making GET call to '%s'.", url);

                // TODO - add headers
                
                final AuthorizationCodeFlow flow = getFlow(); // TODO - handle null flow
                final Credential credentials = loadCredentials(flow);
                final HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credentials);
                final GenericUrl requestUrl = new GenericUrl(url);

                try {
                    final HttpRequest request = requestFactory.buildGetRequest(requestUrl);
                    final HttpResponse response = request.execute();
                    if (listener != null) {
                        final int statusCode = response.getStatusCode();
                        final String contentType = response.getContentType();
                        final InputStream inputStream = response.getContent();
                        listener.onSuccess(statusCode, contentType, inputStream);
                    }
                } catch (com.google.api.client.http.HttpResponseException e) {

                    // TODO - Check for an HttpResponseException indicating that the token has expired.
                    Logger.ex("Could not get user info", e);
                    if (listener != null) {
                        listener.onFailure(e.getLocalizedMessage());
                    }

                } catch (IOException e) {

                    // Some other error occurred?
                    Logger.ex("Could not get user info", e);

                    if (listener != null) {
                        listener.onFailure(e.getLocalizedMessage());
                    }
                }

                return null;
            }
        };
        task.execute();
    }

    private AuthorizationCodeFlow getFlow() {

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

    private Credential loadCredentials(AuthorizationCodeFlow flow) {
        try {
            // TODO - make a new parameter for the user ID.
            final Credential credential = flow.loadCredential(authorizationPreferencesProvider.getClientId());
            return credential;
        } catch (IOException e) {
            Logger.ex("Could not load user credentials", e);
            return null;
        }
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

}
