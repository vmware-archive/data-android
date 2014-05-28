package com.pivotal.cf.mobile.datasdk.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.client.AuthorizationException;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

public class AuthorizedApiRequestImpl implements AuthorizedApiRequest {

    // TODO - should this item should be provided via the constructor (i.e.: dependency injection)?
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static FileDataStoreFactory dataStoreFactory;

    // TODO - these scopes will likely be different in the final product
    private static final String[] SCOPES = new String[]{"profile", "email"};

    // TODO - the state token should be randomly generated, but persisted until the end of the flow
    private static final String STATE_TOKEN = "BLORG";

    private final AuthorizationPreferencesProvider authorizationPreferencesProvider;
    private final ApiProvider apiProvider;
    private AuthorizationCodeFlow flow;

    public AuthorizedApiRequestImpl(Context context,
                                    AuthorizationPreferencesProvider authorizationPreferencesProvider,
                                    ApiProvider apiProvider) throws Exception {

        // TODO - ensure arguments are not null
        // TODO - once arguments have settled down, make separate methods to verify and save them.

        this.authorizationPreferencesProvider = authorizationPreferencesProvider;
        this.apiProvider = apiProvider;
        setupDataStore(context);
        setupFlow();
    }

    private void setupDataStore(Context context) {
        if (dataStoreFactory == null) {
            final File dataStoreDir = context.getDir("oauth2", Context.MODE_PRIVATE);
            try {
                dataStoreFactory = new FileDataStoreFactory(dataStoreDir);
            } catch (IOException e) {
                // TODO - pass errors back via callback
                Logger.ex("Could not open file data store", e);
            }
        }
    }

    private void setupFlow() throws Exception {

        try {
            final String clientId = authorizationPreferencesProvider.getClientId();
            final String clientSecret = authorizationPreferencesProvider.getClientSecret();
            final URL authorizationURL = authorizationPreferencesProvider.getAuthorizationUrl();
            final URL tokenUrl = authorizationPreferencesProvider.getTokenUrl();

            if (clientId == null || clientSecret == null || authorizationURL == null || tokenUrl == null) {
                throw new AuthorizationException("Authorization preferences have not been set up.");
            }

            flow = new AuthorizationCodeFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    apiProvider.getTransport(),
                    JSON_FACTORY,
                    new GenericUrl(tokenUrl),
                    new ClientParametersAuthentication(clientId, clientSecret),
                    clientId,
                    authorizationURL.toString())
                    .setScopes(Arrays.asList(SCOPES))
                    .setDataStoreFactory(dataStoreFactory).build();

        } catch (IOException e) {
            // TODO - pass errors back via callback
            Logger.ex("Could not create AuthorizationCodeFlow object", e);
            throw new AuthorizationException("Could not create AuthorizationCodeFlow: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void obtainAuthorization(Activity activity, DataParameters parameters) {
        final String url = getAuthorizationRequestUrl(parameters);
        Logger.fd("Loading authorization request URL to identify server in external browser: '%s'.", url);
        final Uri uri = Uri.parse(url);
        final Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivity(i); // Launches external browser to do complete authentication
    }

    private String getAuthorizationRequestUrl(DataParameters parameters) {
        final AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl();
        authorizationUrl.setRedirectUri(parameters.getRedirectUrl().toString());
        authorizationUrl.setState(STATE_TOKEN);
        return authorizationUrl.build();
    }

    @Override
    public void getAccessToken(final String authorizationCode, final AuthorizationListener listener) {
        // TODO - remove the AsyncTask after the thread pool is set up.

        final AsyncTask<Void, Void, TokenResponse> task = new AsyncTask<Void, Void, TokenResponse>() {

            @Override
            protected TokenResponse doInBackground(Void... params) {
                try {
                    final AuthorizationCodeTokenRequest tokenUrl = flow.newTokenRequest(authorizationCode);
                    tokenUrl.setRedirectUri(authorizationPreferencesProvider.getRedirectUrl().toString());
                    return tokenUrl.execute();
                } catch (Exception e) {
                    Logger.ex("Could not get tokens.", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(TokenResponse tokenResponse) {
                if (tokenResponse != null) {
                    Logger.fd("Received access token from identity server: '%s'.", tokenResponse.getAccessToken());
                    Logger.d("Authorization flow complete.");
                    listener.onSuccess(tokenResponse);
                } else {
                    Logger.e("Got null token response.");
                    // TODO - report failure to callback - provide a better error message
                    listener.onFailure("Got null token response.");
                }
            }

        };
        task.execute((Void) null);
    }

    @Override
    public void get(URL url,
                    Map<String, Object> headers,
                    Credential credential,
                    AuthorizationPreferencesProvider authorizationPreferencesProvider,
                    HttpOperationListener listener) {

        final HttpRequestFactory requestFactory = apiProvider.getFactory(credential);
        final GenericUrl requestUrl = new GenericUrl(url);

        try {
            final HttpRequest request = requestFactory.buildGetRequest(requestUrl);
            addHeadersToRequest(headers, request);
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
    }

    private void addHeadersToRequest(Map<String, Object> headers, HttpRequest request) {
        if (headers != null) {
            for(final Map.Entry<String, Object> entry : headers.entrySet()) {
                if (entry != null) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        request.getHeaders().set(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }

    public void storeTokenResponse(TokenResponse tokenResponse) {
        try {
            // TODO - make a new parameter for the user ID.
            flow.createAndStoreCredential(tokenResponse, authorizationPreferencesProvider.getClientId());
        } catch (IOException e) {
            Logger.ex("Could not store token response", e);
        }
    }

    public Credential loadCredential() {
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
