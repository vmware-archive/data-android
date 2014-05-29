package com.pivotal.cf.mobile.datasdk.client;

import android.app.Activity;
import android.content.Context;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.DataParameters;
import com.pivotal.cf.mobile.datasdk.activity.BaseAuthorizationActivity;
import com.pivotal.cf.mobile.datasdk.api.ApiProvider;
import com.pivotal.cf.mobile.datasdk.api.AuthorizedApiRequest;
import com.pivotal.cf.mobile.datasdk.prefs.AuthorizationPreferencesProvider;

public class AuthorizationEngine extends AbstractAuthorizationClient {

    public AuthorizationEngine(Context context,
                               ApiProvider apiProvider,
                               AuthorizationPreferencesProvider authorizationPreferencesProvider) {

        super(context, apiProvider, authorizationPreferencesProvider);
    }

    /**
     * Starts the authorization process.
     *
     * @param activity   an already-running activity to use as the base of the authorization process.  This activity
     *                   *MUST* have an intent filter in the AndroidManifest.xml file that captures the redirect URL
     *                   sent by the server.  e.g.:
     *                   <intent-filter>
     *                      <action android:name="android.intent.action.VIEW" />
     *                      <category android:name="android.intent.category.DEFAULT" />
     *                      <category android:name="android.intent.category.BROWSABLE" />
     *                      <data android:scheme="YOUR.REDIRECT_URL.SCHEME" />
     *                      <data android:host="YOUR.REDIRECT.URL.HOST_NAME" />
     *                      <data android:pathPrefix="YOUR.REDIRECT.URL.PATH />
     *                   </intent-filter>
     * @param parameters Parameters object defining the client identification and API endpoints used by
     */
    // TODO - needs a callback to report authorization success/failure.
    // TODO - describe thrown exceptions
    public void obtainAuthorization(Activity activity, DataParameters parameters) throws Exception {
        if (activity == null) {
            throw new IllegalArgumentException("activity may not be null");
        }
        verifyDataParameters(parameters);
        saveDataParameters(parameters);
        startAuthorization(activity, parameters);
    }

    private void verifyDataParameters(DataParameters parameters) {
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
        if (parameters.getRedirectUrl() == null) {
            throw new IllegalArgumentException("parameters.redirectUrl may not be null");
        }
    }

    private void saveDataParameters(DataParameters parameters) {
        authorizationPreferencesProvider.setClientId(parameters.getClientId());
        authorizationPreferencesProvider.setClientSecret(parameters.getClientSecret());
        authorizationPreferencesProvider.setAuthorizationUrl(parameters.getAuthorizationUrl());
        authorizationPreferencesProvider.setTokenUrl(parameters.getTokenUrl());
        authorizationPreferencesProvider.setRedirectUrl(parameters.getRedirectUrl());

        // TODO - if these parameters are different from any existing authorization then the existing authorization must be cleared
    }

    private void startAuthorization(Activity activity, DataParameters parameters) throws Exception {

        // Launches external browser to do complete authentication
        final AuthorizedApiRequest request = apiProvider.getAuthorizedApiRequest(context, authorizationPreferencesProvider);
        request.obtainAuthorization(activity, parameters);
    }

    /**
     * Re-entry point to the authorization engine after the user authorizes the application and the
     * server sends back an authorization code.  Calling this method will make the call to the identity
     * server to receive the access token (which is required before calling any protected APIs).
     * This method will fail if it has been called before obtainAuthorization.
     *
     * This method assumes that it is called on the main thread.
     *
     * @param activity          an already-running activity to use as the base of the authorization process.  This activity
     *                          *MUST* have an intent filter in the `AndroidManifest.xml` file that captures the redirect URL
     *                          sent by the server.  Note that the `AuthorizationEngine` will hold a reference to this activity
     *                          until the access token from the identity server has been received and one of the two callbacks
     *                          in the activity have been made.
     *
     * @param authorizationCode the authorization code received from the server.
     */
    // TODO - describe thrown exceptions
    public void authorizationCodeReceived(final BaseAuthorizationActivity activity, final String authorizationCode) throws Exception {

        Logger.fd("Received authorization code from identity server: '%s'.", authorizationCode);

        if (!areAuthorizationPreferencesAvailable()) {
            throw new AuthorizationException("Authorization parameters have not been set.");
        }

        // TODO - ensure that an authorization flow is already active
        final AuthorizedApiRequest request = apiProvider.getAuthorizedApiRequest(context, authorizationPreferencesProvider);
        request.getAccessToken(authorizationCode, new AuthorizedApiRequest.AuthorizationListener() {

            @Override
            public void onSuccess(TokenResponse tokenResponse) {
                if (activity != null) {
                    activity.onAuthorizationComplete();
                }
            }

            @Override
            public void onFailure(String reason) {
                // TODO - should we clear the credentials?
                if (activity != null) {
                    activity.onAuthorizationFailed(reason);
                }
            }
        });
    }

    // TODO - add Javadocs
    public void clearAuthorization(Context context, DataParameters parameters) throws Exception {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        verifyDataParameters(parameters);
        saveDataParameters(parameters);
        final AuthorizedApiRequest request = apiProvider.getAuthorizedApiRequest(context, authorizationPreferencesProvider);
        request.clearSavedCredential();
    }

}
